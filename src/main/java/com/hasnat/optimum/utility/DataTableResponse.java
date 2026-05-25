package com.hasnat.optimum.utility;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * Generic server-side DataTables response envelope.
 *
 * <p>Complies with the jQuery DataTables server-side processing protocol
 * (v1.10 / 2.x). Controllers return this annotated with {@code @ResponseBody}
 * and Spring's Jackson serialiser produces the JSON the plugin expects.
 *
 * <h3>Required response fields (DataTables protocol)</h3>
 * <pre>
 * {
 *   "draw":            1,      ← echoed from request (XSS guard)
 *   "recordsTotal":    250,    ← total rows before any filter
 *   "recordsFiltered": 38,     ← rows after filter (drives pagination)
 *   "data":            [...]   ← current page rows
 * }
 * </pre>
 *
 * <h3>Optional fields</h3>
 * <pre>
 *   "error"   — non-null triggers DataTables' built-in error dialog
 *   "success" — internal API flag (ignored by DataTables)
 *   "message" — human-readable status (ignored by DataTables)
 * </pre>
 *
 * <h3>Recommended usage — use static factories, not the raw builder</h3>
 * <pre>{@code
 * // Normal result
 * return DataTableResponse.of(draw, total, filtered, rows);
 *
 * // No active filter (total == filtered)
 * return DataTableResponse.of(draw, total, rows);
 *
 * // Zero results
 * return DataTableResponse.empty(draw);
 *
 * // Query failed
 * } catch (Exception e) {
 *     return DataTableResponse.error(draw, e.getMessage());
 * }
 * }</pre>
 *
 * <p>The raw {@code builder()} is available for edge cases where you need
 * partial field control, but it skips validation — prefer the factories.
 *
 * @param <T> Row type — typically {@code Map<String, Object>} for JdbcTemplate
 *            queries, or a typed DTO for JPA projections.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataTableResponse<T> {

    // ─────────────────────────────────────────────────────────────────────────
    // Required DataTables protocol fields
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Echo of the {@code draw} request parameter.
     * DataTables matches responses to requests with this counter and uses it
     * to guard against JSON-injection (XSS). Must be echoed unchanged.
     */
    @JsonProperty("draw")
    private final int draw;

    /**
     * Total records in the data set before any search/filter is applied.
     * Shown in the "Showing X of <b>N</b> entries" text.
     */
    @JsonProperty("recordsTotal")
    private final long recordsTotal;

    /**
     * Total records after applying the current search/filter.
     * Drives pagination controls. Equals {@link #recordsTotal} when no
     * filter is active. Must be ≤ {@code recordsTotal}.
     */
    @JsonProperty("recordsFiltered")
    private final long recordsFiltered;

    /**
     * Row data for the current page. Never null in a valid response.
     */
    @JsonProperty("data")
    private final List<T> data;

    // ─────────────────────────────────────────────────────────────────────────
    // Optional DataTables protocol field
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Error message shown by DataTables in an alert dialog.
     * Any non-null value stops normal rendering and shows the error.
     * Omitted from JSON when null.
     */
    @JsonProperty("error")
    private final String error;

    // ─────────────────────────────────────────────────────────────────────────
    // Internal extension fields — consistent with the rest of the API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Internal API success flag — ignored by DataTables but read by our
     * JS error handlers. Omitted from JSON when null.
     */
    @JsonProperty("success")
    private final Boolean success;

    /**
     * Human-readable status — omitted from JSON when null.
     */
    @JsonProperty("message")
    private final String message;

    // ─────────────────────────────────────────────────────────────────────────
    // Static factory methods  (preferred over the raw builder)
    //
    // Validation lives here, not in a custom builder override.
    // Overriding Lombok's generated builder class body is unreliable across
    // Lombok versions — the injected fields may not be visible to hand-written
    // methods, causing "cannot find symbol: variable data" compile errors.
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Standard result with explicit total and filtered counts.
     * Use when a search/filter is active (total ≠ filtered).
     *
     * @param draw            echo from request
     * @param recordsTotal    unfiltered row count
     * @param recordsFiltered filtered row count (≤ recordsTotal)
     * @param data            current page rows (null → empty list)
     */
    public static <T> DataTableResponse<T> of(
            int    draw,
            long   recordsTotal,
            long   recordsFiltered,
            List<T> data) {

        validate(draw, recordsTotal, recordsFiltered);

        return DataTableResponse.<T>builder()
                .draw(draw)
                .recordsTotal(recordsTotal)
                .recordsFiltered(Math.min(recordsFiltered, recordsTotal)) // guard
                .data(data != null ? data : Collections.emptyList())
                .success(true)
                .build();
    }

    /**
     * Convenience overload — no active filter, so total == filtered.
     *
     * @param draw         echo from request
     * @param recordsTotal total and filtered row count
     * @param data         current page rows (null → empty list)
     */
    public static <T> DataTableResponse<T> of(
            int    draw,
            long   recordsTotal,
            List<T> data) {

        return of(draw, recordsTotal, recordsTotal, data);
    }

    /**
     * Empty result — zero rows, zero totals.
     * Use for early-return guards when a required context is missing.
     *
     * <pre>{@code
     * if (warehouseId == null) return DataTableResponse.empty(draw);
     * }</pre>
     */
    public static <T> DataTableResponse<T> empty(int draw) {
        return DataTableResponse.<T>builder()
                .draw(draw)
                .recordsTotal(0L)
                .recordsFiltered(0L)
                .data(Collections.emptyList())
                .success(true)
                .build();
    }

    /**
     * Error result — DataTables displays a native alert dialog with the message.
     * Sets {@code data} to an empty list so the table renders safely.
     *
     * <pre>{@code
     * } catch (Exception e) {
     *     return DataTableResponse.error(draw, "Query failed: " + e.getMessage());
     * }
     * }</pre>
     */
    public static <T> DataTableResponse<T> error(int draw, String errorMessage) {
        return DataTableResponse.<T>builder()
                .draw(draw)
                .recordsTotal(0L)
                .recordsFiltered(0L)
                .data(Collections.emptyList())
                .error(errorMessage)
                .success(false)
                .message(errorMessage)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Internal validation — called from factory methods only
    // ─────────────────────────────────────────────────────────────────────────

    private static void validate(int draw, long recordsTotal, long recordsFiltered) {
        if (draw < 0) {
            throw new IllegalArgumentException(
                "DataTableResponse.draw must be >= 0, got: " + draw);
        }
        if (recordsTotal < 0) {
            throw new IllegalArgumentException(
                "DataTableResponse.recordsTotal must be >= 0, got: " + recordsTotal);
        }
        if (recordsFiltered < 0) {
            throw new IllegalArgumentException(
                "DataTableResponse.recordsFiltered must be >= 0, got: " + recordsFiltered);
        }
        // recordsFiltered > recordsTotal is clamped in of(), not thrown,
        // because COUNT(*) vs COUNT(DISTINCT id) on a multi-join can hit this
        // in a race condition — crashing the page would be worse than clamping.
    }
}
