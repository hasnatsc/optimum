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
 * (v1.10 / 2.x). The controller simply returns this object annotated with
 * {@code @ResponseBody} and Spring's Jackson serializer produces the JSON
 * the DataTable plugin expects.
 *
 * <h3>Required response fields</h3>
 * <pre>
 * {
 *   "draw":            1,           ← echo the request draw int (XSS guard)
 *   "recordsTotal":    250,         ← total rows in table before any filter
 *   "recordsFiltered": 38,          ← rows after search/filter (drives pagination)
 *   "data":            [ ... ]      ← array of row objects
 * }
 * </pre>
 *
 * <h3>Optional fields</h3>
 * <pre>
 *   "error"   — non-null triggers the DataTables error dialog
 *   "success" — internal API consistency flag (ignored by DataTables)
 *   "message" — human-readable status (ignored by DataTables)
 * </pre>
 *
 * <h3>Usage — success path</h3>
 * <pre>{@code
 * return DataTableResponse.<Map<String, Object>>builder()
 *     .draw(draw)
 *     .recordsTotal(totalCount)
 *     .recordsFiltered(filteredCount)
 *     .data(rows)
 *     .build();
 * }</pre>
 *
 * <h3>Usage — error path</h3>
 * <pre>{@code
 * return DataTableResponse.error(draw, "Database connection failed.");
 * }</pre>
 *
 * <h3>Usage — empty result</h3>
 * <pre>{@code
 * return DataTableResponse.empty(draw);
 * }</pre>
 *
 * @param <T> Row type — typically {@code Map<String, Object>} for ad-hoc
 *            JdbcTemplate queries, or a typed DTO for JPA projections.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)  // omit null fields (error, message) from JSON
public class DataTableResponse<T> {

    // ─────────────────────────────────────────────────────────────────────────
    // Required DataTables protocol fields
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Echo of the {@code draw} request parameter.
     * <p>DataTables uses this counter to match responses to requests and to
     * prevent cross-site scripting attacks via JSON injection. You MUST echo
     * it back unchanged.
     */
    @JsonProperty("draw")
    private final int draw;

    /**
     * Total number of records in the underlying data set (before any filter).
     * <p>Drives the "Showing X of N entries" display text.
     */
    @JsonProperty("recordsTotal")
    private final long recordsTotal;

    /**
     * Total number of records after applying the current search/filter.
     * <p>Drives pagination controls. When there is no active filter this
     * equals {@link #recordsTotal}.
     */
    @JsonProperty("recordsFiltered")
    private final long recordsFiltered;

    /**
     * The row data for the current page.
     * <p>Never null — defaults to an empty list via the builder default.
     */
    @JsonProperty("data")
    @Builder.Default
    private final List<T> data = Collections.emptyList();

    // ─────────────────────────────────────────────────────────────────────────
    // Optional DataTables protocol field
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Error message displayed by DataTables in an alert dialog.
     * <p>Set this (and leave {@link #data} empty) when the query fails.
     * The plugin treats any non-null value as a hard error and stops rendering.
     * Omitted from JSON when null ({@code @JsonInclude(NON_NULL)}).
     */
    @JsonProperty("error")
    private final String error;

    // ─────────────────────────────────────────────────────────────────────────
    // Internal extension fields — consistent with the rest of the API
    // (ignored by DataTables but read by our own JS error handlers)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Internal API success flag.
     * <p>{@code true} for normal responses, {@code false} when
     * {@link #error} is set. Omitted from JSON when null.
     */
    @JsonProperty("success")
    private final Boolean success;

    /**
     * Human-readable status message for the front-end.
     * Omitted from JSON when null.
     */
    @JsonProperty("message")
    private final String message;

    // ─────────────────────────────────────────────────────────────────────────
    // Static factory methods — preferred over the raw builder for common cases
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Standard success response — all fields explicit.
     *
     * <pre>{@code
     * DataTableResponse.of(draw, totalCount, filteredCount, rows)
     * }</pre>
     */
    public static <T> DataTableResponse<T> of(
            int draw, long recordsTotal, long recordsFiltered, List<T> data) {

        return DataTableResponse.<T>builder()
            .draw(draw)
            .recordsTotal(recordsTotal)
            .recordsFiltered(recordsFiltered)
            .data(data != null ? data : Collections.emptyList())
            .success(true)
            .build();
    }

    /**
     * Convenience overload — recordsTotal == recordsFiltered (no search active).
     *
     * <pre>{@code
     * DataTableResponse.of(draw, totalCount, rows)
     * }</pre>
     */
    public static <T> DataTableResponse<T> of(
            int draw, long recordsTotal, List<T> data) {
        return of(draw, recordsTotal, recordsTotal, data);
    }

    /**
     * Empty page — zero rows, zero totals. Useful for early-return guards.
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
     * Error response — DataTables will display an alert dialog with the message.
     *
     * <pre>{@code
     * } catch (Exception e) {
     *     return DataTableResponse.error(draw, "Failed to load data: " + e.getMessage());
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
    // Builder override — ensures data is never null even if not set
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Custom builder — enforces the DataTables contract at build time.
     *
     * <ul>
     *   <li>{@code draw} must be ≥ 0 (DataTables starts at 1; 0 is safe for manual tests).
     *   <li>{@code recordsTotal} must be ≥ 0.
     *   <li>{@code recordsFiltered} must be ≤ {@code recordsTotal}.
     *   <li>{@code data} is defaulted to an empty list if null.
     * </ul>
     */
    public static class DataTableResponseBuilder<T> {

        /**
         * Override Lombok's build() to add validation.
         * Lomboks @Builder generates this method — we override it to add guards.
         */
        public DataTableResponse<T> build() {

            // Default data to empty list
            if (this.data == null) {
                this.data = Collections.emptyList();
            }

            // Contract enforcement
            if (this.draw < 0) {
                throw new IllegalStateException("DataTableResponse.draw must be >= 0");
            }
            if (this.recordsTotal < 0) {
                throw new IllegalStateException("DataTableResponse.recordsTotal must be >= 0");
            }
            if (this.recordsFiltered < 0) {
                throw new IllegalStateException("DataTableResponse.recordsFiltered must be >= 0");
            }
            if (this.recordsFiltered > this.recordsTotal) {
                // recordsFiltered > recordsTotal breaks DataTables pagination.
                // Clamp silently rather than throwing, since COUNT(*) / COUNT(DISTINCT)
                // inconsistencies can cause this in rare race conditions.
                this.recordsFiltered = this.recordsTotal;
            }

            // Default success flag based on whether an error was set
            if (this.success == null) {
                this.success = (this.error == null);
            }

            return new DataTableResponse<>(
                this.draw,
                this.recordsTotal,
                this.recordsFiltered,
                this.data,
                this.error,
                this.success,
                this.message
            );
        }
    }
}
