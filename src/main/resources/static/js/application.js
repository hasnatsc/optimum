/**
 * application.js  — Spindles ERP global utility library
 *
 * ─────────────────────────────────────────────────────────────────────────
 * TABLE OF CONTENTS
 * ─────────────────────────────────────────────────────────────────────────
 *  §1   DOCUMENT-READY INITIALISERS  (select2, datepicker, datetimepicker)
 *  §2   CORE NETWORK               (secureFetch, ajaxRequest [legacy])
 *  §3   NOTIFICATION HELPERS       (hsShowToast, hsServerError)
 *  §4   FORM HELPERS               (objectifyForm, hsResetForm, hsDisableButton…)
 *  §5   MODAL HELPERS              (hsOpenModal, hsOpenModalForm, hsFetchAndShowModal)
 *  §6   DATATABLE HELPERS          (hsInitDataTable, hsAfterSave, hsAfterSaveMessages)
 *  §7   DELETE / CONFIRM ACTIONS   (confirmAndExecute, hsConfirmDelete)
 *  §8   STATUS / APPROVAL ACTIONS  (hsChangeStatus, doTransition helpers)
 *  §9   SELECT2 HELPERS            (selectTwoAjaxInitCall, S2HSHelper, …)
 *  §10  TABLE RENDER HELPERS       (hs_add_table_data, hs_view_table_data, …)
 *  §11  CURRENCY / DATE UTILITIES  (formatCurrency, hsFormatDate, dateDiff…)
 *  §12  DOM UTILITIES              (hsSetText, hsSetHtml, nullCheck, isItNull…)
 *  §13  IMPORT / EXPORT HELPERS    (importData, asgLoadDropdown)
 *  §14  ACTION HANDLER MODULE      (window.ActionHandler)
 *  §15  ADVANCED SUBMIT HELPERS    (submitWithSecureFetch, hsInitAjaxForm)
 *  §16  LEGACY / DEPRECATED        (gritter wrappers kept for backward-compat)
 * ─────────────────────────────────────────────────────────────────────────
 */

'use strict';

/* ═══════════════════════════════════════════════════════════════════════════
   §1  DOCUMENT-READY INITIALISERS
   ═══════════════════════════════════════════════════════════════════════════ */

$(document).ready(function () {

    /* Auto-init select2 on elements with class .hsSelectTwo */
    $('.hsSelectTwo').each(function () {
        $(this).select2({
            dropdownParent: $(this).parent(),
            placeholder:    'Select one…',
            allowClear:     true
        });
    });

    /* Date picker — dd-mm-yyyy */
    $('.hsPickDate').datepicker({
        format:         'dd-mm-yyyy',
        autoclose:      true,
        todayHighlight: true,
        todayBtn:       'linked',
        tooltip:        false
    }).on('changeDate', function () {
        $(this).datepicker('hide');
        $(this).parsley().validate();
    });

    /* DateTime picker */
    $('.hsPickDateTime').datetimepicker({
        format:          'dd-mm-yyyy hh:mm',
        showTodayButton: true,
        showClear:       true,
        showClose:       true,
        sideBySide:      true,
        icons: {
            time:     'fa fa-clock',
            date:     'fa fa-calendar',
            up:       'fa fa-chevron-up',
            down:     'fa fa-chevron-down',
            previous: 'fa fa-chevron-left',
            next:     'fa fa-chevron-right',
            today:    'fa fa-crosshairs',
            clear:    'fa fa-trash',
            close:    'fa fa-check'
        }
    });
});

/**
 * Initialise a datepicker on a given CSS class/selector.
 * @param {string} refClass
 */
function initDatefield(refClass) {
    $(refClass).datepicker({
        format:         'dd-mm-yyyy',
        autoclose:      true,
        todayHighlight: true,
        todayBtn:       'linked',
        tooltip:        false
    }).on('changeDate', function () {
        $(this).datepicker('hide');
        $(this).parsley().validate();
    });
}


/* ═══════════════════════════════════════════════════════════════════════════
   §2  CORE NETWORK
   ═══════════════════════════════════════════════════════════════════════════ */

/**
 * Secure fetch wrapper — attaches CSRF, sets Content-Type for JSON bodies,
 * and normalises the response into a plain object.
 *
 * @param {string}  url
 * @param {object}  [options={}]  Standard fetch options
 * @returns {Promise<object>}
 */
window.secureFetch = async function secureFetch(url, options = {}) {
    const method  = (options.method || 'GET').toUpperCase();
    const headers = new Headers(options.headers || {});

    const csrfToken  = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    // Auto-set JSON content-type for non-FormData, non-URLSearchParams bodies
    if (
        options.body &&
        !(options.body instanceof FormData) &&
        !(options.body instanceof URLSearchParams) &&
        !headers.has('Content-Type')
    ) {
        headers.set('Content-Type', 'application/json');
    }

    // Attach CSRF for mutating requests
    if (method !== 'GET' && csrfToken && csrfHeader) {
        headers.set(csrfHeader, csrfToken);
    }

    const response = await fetch(url, {
        credentials: 'same-origin',
        ...options,
        method,
        headers
    });

    if (response.status === 204) {
        return { success: true, message: 'Operation completed successfully' };
    }

    const contentType = response.headers.get('content-type') || '';

    if (contentType.includes('application/json')) {
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || 'Request failed');
        return data;
    }

    const text = await response.text();
    if (!response.ok) throw new Error(text || 'Request failed');
    return { success: true, message: text };
};

/**
 * Legacy synchronous AJAX wrapper.
 * ⚠️  Deprecated — synchronous XHR is deprecated by the browser spec and
 *     blocks the UI thread. Migrate callers to secureFetch() progressively.
 *
 * @param  {string} actions     URL
 * @param  {object} parameters  Form data / params
 * @param  {string} [type]      HTTP method (ignored — always POST)
 * @returns {*} Raw response data
 */
function ajaxRequest(actions, parameters, type = 'POST') {
    /* eslint-disable no-alert */
    console.warn('[ajaxRequest] Synchronous XHR is deprecated. Migrate to secureFetch().');
    let defaultObject;
    jQuery.ajax({
        type:      'POST',
        dataType:  'JSON',
        async:     false,           // deprecated — keep for legacy callers only
        data:      parameters,
        url:       actions,
        success:   function (data) { defaultObject = data; },
        error:     function (xhr, status, err) { hsServerError(xhr, status, err); }
    });
    return defaultObject;
}


/* ═══════════════════════════════════════════════════════════════════════════
   §3  NOTIFICATION HELPERS
   ═══════════════════════════════════════════════════════════════════════════ */

/**
 * Show a non-blocking Swal toast notification.
 *
 * @param {'success'|'error'|'warning'|'info'} type
 * @param {string} title
 * @param {string} [message]
 * @param {number} [timer=2500]
 */
function hsShowToast(type, title, message, timer = 2500) {
    Swal.fire({
        icon:              type,
        title:             title,
        text:              message || '',
        toast:             true,
        position:          'top-end',
        timer:             timer,
        showConfirmButton: false,
        timerProgressBar:  true
    });
}

/**
 * Handle server-side AJAX errors consistently.
 * Previously used $.gritter — migrated to Swal.fire().
 *
 * @param {XMLHttpRequest} xhr
 * @param {string}         textStatus
 * @param {string}         errorThrown
 */
function hsServerError(xhr, textStatus, errorThrown) {
    if (xhr.status === 401) {
        // Session expired — reload to login page
        window.location.reload();
        return;
    }

    let msg;
    switch (xhr.status) {
        case 0:   msg = 'Network unreachable. Please check your connection.'; break;
        case 404: msg = 'Requested resource not found (404).';                break;
        case 500: msg = 'Internal Server Error (500). Please try again.';     break;
        default:
            if (textStatus === 'parsererror') msg = 'Response JSON parse failed.';
            else if (textStatus === 'timeout')  msg = 'Request timed out.';
            else if (textStatus === 'abort')    msg = 'Request was aborted.';
            else {
                try { msg = JSON.parse(xhr.responseText).error || errorThrown; }
                catch (e) { msg = errorThrown || 'Unknown server error.'; }
            }
    }

    console.error('[hsServerError]', xhr.status, textStatus, errorThrown);
    Swal.fire({ icon: 'error', title: 'Server Error', text: msg });
}


/* ═══════════════════════════════════════════════════════════════════════════
   §4  FORM HELPERS
   ═══════════════════════════════════════════════════════════════════════════ */

/**
 * Serialise a <form> element into a nested plain object.
 * Supports dotted field names (e.g. "address.city" → { address: { city: '…' } }).
 *
 * @param  {HTMLFormElement} form
 * @returns {object}
 */
function objectifyForm(form) {
    const formData = {};
    new FormData(form).forEach((value, key) => {
        if (key.includes('.')) {
            const parts = key.split('.');
            let obj = formData;
            for (let i = 0; i < parts.length - 1; i++) {
                obj[parts[i]] = obj[parts[i]] || {};
                obj = obj[parts[i]];
            }
            obj[parts[parts.length - 1]] = value || '';
        } else {
            formData[key] = value || '';
        }
    });
    return formData;
}

/**
 * Reset a form: native reset + clear hidden inputs + reset all select2.
 * @param {HTMLFormElement} form
 */
function hsResetForm(form) {
    form.reset();
    form.querySelectorAll('input[type="hidden"]').forEach(el => el.value = '');
    $(form).find('select').val('').trigger('change');
}

/**
 * Disable a submit button and show a spinner.
 * @param {HTMLButtonElement} submitBtn
 * @param {HTMLElement}       spinner
 * @param {HTMLElement}       btnText
 * @param {string}            [loadingLabel='Processing…']
 */
function hsDisableButton(submitBtn, spinner, btnText, loadingLabel = 'Processing…') {
    submitBtn.disabled = true;
    spinner.classList.remove('d-none');
    btnText.textContent = loadingLabel;
}

/**
 * Re-enable a submit button after a save attempt.
 * @param {HTMLButtonElement} submitBtn
 * @param {HTMLElement}       spinner
 * @param {HTMLElement}       btnText
 * @param {string}            [label='Save']
 */
function hsEnableButton(submitBtn, spinner, btnText, label = 'Save') {
    submitBtn.disabled = false;
    spinner.classList.add('d-none');
    btnText.textContent = label;
}

/**
 * Disable a button by element ID (adds disabled + visual cues).
 * @param {string} id
 */
function hsDisableBtn(id) {
    const btn = document.getElementById(id);
    if (!btn) return;
    btn.disabled      = true;
    btn.style.opacity = '0.6';
    btn.style.cursor  = 'not-allowed';
    btn.classList.add('disabled');
}

/**
 * Re-enable a button by element ID.
 * @param {string} id
 */
function hsEnableBtn(id) {
    const btn = document.getElementById(id);
    if (!btn) return;
    btn.disabled      = false;
    btn.style.opacity = '';
    btn.style.cursor  = '';
    btn.classList.remove('disabled');
}


/* ═══════════════════════════════════════════════════════════════════════════
   §5  MODAL HELPERS
   ═══════════════════════════════════════════════════════════════════════════ */

/**
 * Open a Bootstrap 5 modal.
 * @param {HTMLElement|string} modal  Element or element ID string
 */
function hsOpenModal(modal) {
    const el = typeof modal === 'string' ? document.getElementById(modal) : modal;
    bootstrap.Modal.getOrCreateInstance(el).show();
}

/**
 * Open a modal and reset its form.
 * @param {string} modalId
 * @param {string} formId
 */
function hsOpenModalForm(modalId, formId) {
    hsResetForm(document.getElementById(formId));
    hsOpenModal(modalId);
}

/**
 * Fetch JSON from a URL, invoke a fill callback, then open a modal.
 *
 * @param {string}   url
 * @param {string}   modalId
 * @param {Function} fillCallback  Receives the parsed JSON response
 */
function hsFetchAndShowModal(url, modalId, fillCallback) {
    secureFetch(url)
        .then(data => {
            fillCallback(data);
            hsOpenModal(modalId);
        })
        .catch(err => {
            console.error('[hsFetchAndShowModal]', err);
            Swal.fire({ icon: 'error', title: 'Error', text: 'Failed to load data.' });
        });
}


/* ═══════════════════════════════════════════════════════════════════════════
   §6  DATATABLE HELPERS
   ═══════════════════════════════════════════════════════════════════════════ */

/**
 * Initialise a server-side DataTable with standard options.
 *
 * @param {string} selector   CSS selector for the <table>
 * @param {string} ajaxUrl
 * @param {Array}  columns    DataTables column definitions
 * @param {object} [extra]    Additional DataTable options to merge
 * @returns {DataTable}
 */
function hsInitDataTable(selector, ajaxUrl, columns, extra = {}) {
    return $(selector).DataTable({
        processing: true,
        responsive: true,
        serverSide: true,
        ajax:       { url: ajaxUrl, type: 'GET' },
        columns,
        ...extra
    });
}

/**
 * Reload a DataTable instance safely — accepts either a DataTable API
 * instance or a DOM element / jQuery selector.
 *
 * @param {DataTable|HTMLElement|string} table
 */
function _reloadDataTable(table) {
    if (!table) return;
    // Already a DataTable API instance
    if (table && typeof table.ajax === 'object' && typeof table.ajax.reload === 'function') {
        table.ajax.reload(null, false);
        return;
    }
    // DOM element or selector — wrap with jQuery
    try { $(table).DataTable().ajax.reload(null, false); }
    catch (e) { console.warn('[_reloadDataTable] Could not reload:', e); }
}

/**
 * Show a save-result alert in a container element and optionally reload a table.
 * FIX: guarded against null `dataTables` crash.
 *
 * @param {string}                        messages
 * @param {boolean}                       success
 * @param {HTMLFormElement|null}          form
 * @param {DataTable|HTMLElement|null}    dataTables
 * @param {string}                        [customSuccessMessages]  Fallback container ID
 */
function hsAfterSaveMessages(messages, success, form, dataTables, customSuccessMessages) {
    const container = document.getElementById('successMessages')
                   || (customSuccessMessages ? document.getElementById(customSuccessMessages) : null);

    if (container) {
        const alert = document.createElement('div');
        alert.className = `alert alert-${success ? 'success' : 'danger'} alert-dismissible fade show`;
        alert.innerHTML = `${messages} <button type="button" class="btn-close" data-bs-dismiss="alert"></button>`;
        container.appendChild(alert);
        setTimeout(() => alert.remove(), 5000);
    }

    if (success) {
        if (form) {
            form.reset();
            form.querySelectorAll('input[type="hidden"]').forEach(el => el.value = '');
            $(form).find('select').val('').trigger('change');
        }
        if (typeof hsCustomForm === 'function') hsCustomForm();
        // FIX: null guard — don't crash if no table passed
        if (dataTables) _reloadDataTable(dataTables);
    }
}

/**
 * Reload a DataTable after a save operation (legacy gritter-based helper).
 * @param {object}           hsData     Response with `.isError` + `.message`
 * @param {HTMLElement}      listTable  Table element (not DataTable instance)
 */
function hsAfterSave(hsData, listTable) {
    hsShowToast(hsData.isError ? 'error' : 'success', hsData.message);
    if (!hsData.isError) {
        $(listTable).DataTable().ajax.reload(null, false);
    }
}


/* ═══════════════════════════════════════════════════════════════════════════
   §7  DELETE / CONFIRM ACTIONS
   ═══════════════════════════════════════════════════════════════════════════ */

/**
 * Centralised confirmation → secureFetch → feedback + optional table reload.
 *
 * @param {object} opts
 * @param {string}                        opts.title
 * @param {string}                        opts.text
 * @param {string}                        [opts.icon='warning']
 * @param {string}                        [opts.confirmText='Yes, proceed!']
 * @param {string}                        [opts.cancelText='Cancel']
 * @param {string}                        [opts.confirmColor='#3085d6']
 * @param {string}                        [opts.cancelColor='#d33']
 * @param {string}                        opts.url
 * @param {string}                        [opts.method='DELETE']
 * @param {object|null}                   [opts.body]
 * @param {string}                        [opts.successTitle='Success!']
 * @param {string|null}                   [opts.successMessage]
 * @param {boolean}                       [opts.reloadTable=false]
 * @param {DataTable|HTMLElement|null}    [opts.dataTable]
 */
window.confirmAndExecute = function confirmAndExecute({
    title         = 'Are you sure?',
    text          = "You won't be able to revert this!",
    icon          = 'warning',
    confirmText   = 'Yes, proceed!',
    cancelText    = 'Cancel',
    confirmColor  = '#3085d6',
    cancelColor   = '#d33',
    url,
    method        = 'DELETE',
    body          = null,
    successTitle  = 'Success!',
    successMessage = null,
    reloadTable   = false,
    dataTable     = null
}) {
    Swal.fire({
        title,
        text,
        icon,
        showCancelButton:   true,
        confirmButtonColor: confirmColor,
        cancelButtonColor:  cancelColor,
        confirmButtonText:  confirmText,
        cancelButtonText:   cancelText
    }).then(result => {
        if (!result.isConfirmed) return;

        secureFetch(url, {
            method,
            body: body ? JSON.stringify(body) : null
        })
        .then(data => {
            if (!data.success) throw new Error(data.message || 'Operation failed');

            Swal.fire({
                icon:              'success',
                title:             successTitle,
                text:              successMessage || data.message,
                timer:             2000,
                showConfirmButton: false
            });

            if (reloadTable && dataTable) _reloadDataTable(dataTable);
        })
        .catch(err => {
            Swal.fire({ icon: 'error', title: 'Error!', text: err.message });
        });
    });
};

/**
 * Simple delete confirmation using confirmAndExecute.
 *
 * @param {string}               url
 * @param {string|HTMLElement}   reloadTableSelector  DataTable element or selector
 */
function hsConfirmDelete(url, reloadTableSelector) {
    confirmAndExecute({
        title:        'Are you sure?',
        text:         'This action cannot be undone!',
        confirmText:  'Yes, delete it!',
        confirmColor: '#d33',
        url,
        method:       'DELETE',
        successTitle: 'Deleted!',
        reloadTable:  true,
        dataTable:    reloadTableSelector
    });
}


/* ═══════════════════════════════════════════════════════════════════════════
   §8  STATUS / APPROVAL ACTIONS
   ═══════════════════════════════════════════════════════════════════════════ */

/**
 * Toggle active/inactive status with confirmation.
 *
 * @param {string}               url
 * @param {string|HTMLElement}   tableSelector   DataTable element or selector
 * @param {string}               [entityName]
 */
function hsChangeStatus(url, tableSelector, entityName = 'Item') {
    Swal.fire({
        title:              `Change ${entityName} Status?`,
        text:               `This will toggle the ${entityName.toLowerCase()} status.`,
        icon:               'question',
        showCancelButton:   true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor:  '#d33',
        confirmButtonText:  'Yes, change it!'
    }).then(result => {
        if (!result.isConfirmed) return;
        secureFetch(url, { method: 'POST' })
            .then(data => {
                if (data.success) {
                    hsShowToast('success', 'Status Changed!', data.message);
                    if (tableSelector) _reloadDataTable(tableSelector);
                } else {
                    Swal.fire({ icon: 'error', title: 'Error!', text: data.message });
                }
            })
            .catch(err => Swal.fire({ icon: 'error', title: 'Error!', text: err.message }));
    });
}

/**
 * Approval action dialog — shows a remarks textarea, then POSTs JSON to endpoint.
 * FIX: body sent as JSON (not URLSearchParams) to match @RequestBody on server.
 * FIX: uses secureFetch() instead of raw fetch().
 *
 * @param {object} opts
 * @param {string}       opts.title
 * @param {string}       opts.icon
 * @param {string}       opts.confirmText
 * @param {string}       opts.confirmColor
 * @param {string}       opts.textareaLabel         (currently unused in HTML but kept for API compat)
 * @param {string}       opts.textareaPlaceholder
 * @param {boolean}      [opts.textareaRequired]
 * @param {string}       opts.endpoint
 * @param {string}       opts.successTitle
 * @param {DataTable|HTMLElement|null} [opts.tableToReload]
 */
function approvalActionDialog({
    title,
    icon,
    confirmText,
    confirmColor,
    textareaLabel,
    textareaPlaceholder,
    textareaRequired = false,
    endpoint,
    successTitle,
    tableToReload
}) {
    Swal.fire({
        title,
        icon,
        html: `<input
                    id="actionRemarks"
                    class="swal2-input"
                    placeholder="${textareaPlaceholder}"
                    maxlength="250">`,
        showCancelButton:  true,
        confirmButtonText: confirmText,
        confirmButtonColor: confirmColor,
        cancelButtonText:  'Cancel',
        focusConfirm:      false,
        preConfirm: () => {
            const remarks = document.getElementById('actionRemarks')?.value.trim();
            if (textareaRequired && !remarks) {
                Swal.showValidationMessage('Remarks are required.');
                return false;
            }
            return { remarks: remarks || '' };
        }
    }).then(result => {
        if (!result.isConfirmed) return;

        Swal.fire({ title: 'Processing…', allowOutsideClick: false, didOpen: () => Swal.showLoading() });

        // FIX: send JSON (not URLSearchParams) to match @RequestBody Map<String,String>
        secureFetch(endpoint, {
            method: 'POST',
            body:   JSON.stringify(result.value)
        })
        .then(data => {
            if (data.success) {
                Swal.fire({ icon: 'success', title: successTitle, text: data.message, timer: 2000, showConfirmButton: false });
                if (tableToReload) _reloadDataTable(tableToReload);
            } else {
                Swal.fire({ icon: 'error', title: 'Error!', text: data.message });
            }
        })
        .catch(() => Swal.fire({ icon: 'error', title: 'Error', text: 'Unexpected server error.' }));
    });
}

/**
 * Remarks + confirm dialog that POSTs to an arbitrary endpoint.
 * FIX: removed dependency on global `dataTables` variable.
 * FIX: uses secureFetch() consistently.
 *
 * @param {object} opts
 * @param {string}       opts.title
 * @param {string}       opts.confirmText
 * @param {string}       opts.confirmColor
 * @param {string}       opts.url
 * @param {string}       [opts.successMessage]
 * @param {DataTable|HTMLElement|null} [opts.tableToReload]   FIX: new param (was global)
 */
function actionWithRemarks({
    title,
    confirmText,
    confirmColor,
    url,
    successMessage = 'Action completed successfully',
    tableToReload  = null     // FIX: was referencing global `dataTables`
}) {
    Swal.fire({
        title,
        html: `
            <div class="mb-3">
                <label class="form-label text-start d-block">Remarks</label>
                <textarea id="actionRemarks"
                          class="form-control"
                          rows="3"
                          placeholder="Enter remarks (optional)"></textarea>
            </div>`,
        showCancelButton:   true,
        confirmButtonColor: confirmColor,
        confirmButtonText:  confirmText,
        preConfirm: () => ({
            remarks: document.getElementById('actionRemarks').value || ''
        })
    }).then(result => {
        if (!result.isConfirmed) return;

        // FIX: use secureFetch() — no more manual CSRF / fetch duplication
        secureFetch(url, {
            method: 'POST',
            body:   JSON.stringify(result.value)
        })
        .then(data => {
            if (data.success) {
                Swal.fire({ icon: 'success', title: 'Success!', text: data.message || successMessage });
                if (tableToReload) _reloadDataTable(tableToReload);
            } else {
                Swal.fire({ icon: 'error', title: 'Error!', text: data.message || 'Action failed.' });
            }
        })
        .catch(() => Swal.fire({ icon: 'error', title: 'Error!', text: 'Server communication failed.' }));
    });
}

/**
 * Trigger a POST action with standard SweetAlert confirm.
 * FIX: removed undefined `dataTable` global reference; caller must pass `tableEl`.
 *
 * @param {object} opts
 * @param {number}       opts.id
 * @param {string}       opts.url
 * @param {string}       opts.title
 * @param {string}       opts.text
 * @param {string}       [opts.icon]
 * @param {string}       [opts.confirmButtonColor]
 * @param {string}       [opts.confirmButtonText]
 * @param {string}       [opts.successTitle]
 * @param {DataTable|HTMLElement|null} [opts.tableEl]  FIX: was using undefined global
 */
function hsPostAction({
    id,
    url,
    title,
    text,
    icon               = 'question',
    confirmButtonColor = '#3085d6',
    confirmButtonText  = 'Yes, proceed!',
    cancelButtonText   = 'Cancel',
    successTitle       = 'Success!',
    tableEl            = null   // FIX: renamed from implicit global `dataTable`
}) {
    Swal.fire({
        title,
        text,
        icon,
        showCancelButton:   true,
        confirmButtonColor,
        cancelButtonColor:  '#d33',
        confirmButtonText,
        cancelButtonText
    }).then(result => {
        if (!result.isConfirmed) return;
        secureFetch(`${url}/${id}`, { method: 'POST' })
            .then(data => {
                if (data.success) {
                    hsShowToast('success', successTitle, data.message);
                    if (tableEl) _reloadDataTable(tableEl);
                } else {
                    Swal.fire({ icon: 'error', title: 'Error!', text: data.message });
                }
            })
            .catch(err => Swal.fire({ icon: 'error', title: 'Error!', text: err.message }));
    });
}


/* ═══════════════════════════════════════════════════════════════════════════
   §9  SELECT2 HELPERS
   ═══════════════════════════════════════════════════════════════════════════ */

/**
 * Initialise a standard AJAX select2 with code/caption display pattern.
 *
 * @param {string}  select2FieldsId
 * @param {string}  actionUrl
 * @param {string}  placeholder
 * @param {boolean} [ajaxSearchParams=true]
 * @param {string}  [modalId]
 * @param {jQuery}  [othersSearchParams]
 * @param {number}  [pageSize=50]               FIX: was magic number 50
 */
function selectTwoAjaxInitCall(
    select2FieldsId,
    actionUrl,
    placeholder,
    ajaxSearchParams = true,
    modalId,
    othersSearchParams,
    pageSize = 50          // FIX: was hard-coded
) {
    $('#' + select2FieldsId).select2({
        dropdownParent: ($('#' + modalId).length ? $('#' + modalId) : null),
        placeholder,
        allowClear:          true,
        width:               '100%',
        minimumInputLength:  0,
        escapeMarkup:        markup => markup,
        ajax: {
            url:      actionUrl,
            dataType: 'json',
            delay:    250,
            data: function (params) {
                return {
                    q:                  params.term,
                    page:               params.page,
                    ajaxSearchParams,
                    othersSearchParams: othersSearchParams ? othersSearchParams.val() : ''
                };
            },
            processResults: function (data, params) {
                params.page = params.page || 1;
                return {
                    results:    data.items,
                    pagination: { more: (params.page * pageSize) < data.total_count }
                };
            },
            cache: true
        },
        templateResult: function (repo) {
            if (repo.loading) return repo.text;
            return '<div class="widget-todolist-item"><div class="widget-todolist-content">' +
                   '<h6 class="mb-2px">' + nullCheck(repo.code) + ' - ' + nullCheck(repo.caption) + '</h6>' +
                   '<div class="text-gray-600 fw-bold fs-11px">' + nullCheck(repo.details) + '</div>' +
                   '</div></div>';
        },
        templateSelection: function (repo) {
            if (repo.id === '')  return placeholder;
            if (repo.code)       return repo.code + ' - ' + repo.caption;
            return repo.text;
        }
    });
}

/**
 * Preselect a value in a select2 element (edit mode helper).
 * @param {string} name     Element ID
 * @param {*}      value    Option value
 * @param {string} caption  Display text
 */
function editSelect2Ajax(name, value, caption) {
    $('#' + name).empty()
        .append(new Option(caption, value, true, true))
        .trigger('change');
}

/**
 * Read the current id + text from a select2.
 * @param  {jQuery} selectedValue  jQuery-wrapped select2 element
 * @returns {{ id: string, value: string }}
 */
function selectTwoIdValues(selectedValue) {
    const data = selectedValue.select2('data');
    if (!isItNull(data) && Object.keys(data).length > 0 && !isItNull(data[0].id)) {
        return { id: data[0].id, value: data[0].text };
    }
    return { id: '', value: '' };
}

/**
 * Populate a linked select2 when another field changes.
 * ⚠️  Uses synchronous AJAX internally — consider migrating to async secureFetch.
 *
 * @param {string}  actionUrl
 * @param {object}  paramsData
 * @param {jQuery}  referenceSelect    Target select2 element
 * @param {boolean} [actionOnlyNotNull]
 * @param {*}       [actionValue]
 * @param {string}  [actionType]
 */
function hsOnChangeSetSelectTwoValue(
    actionUrl,
    paramsData,
    referenceSelect,
    actionOnlyNotNull = false,
    actionValue       = '',
    actionType        = 'POST'
) {
    if (actionOnlyNotNull && isItNull(actionValue)) {
        referenceSelect.select2('destroy').empty().select2({ width: '100%' });
        return;
    }
    jQuery.ajax({
        type:      actionType,
        dataType:  'JSON',
        async:     false,
        data:      paramsData,
        url:       actionUrl,
        success: function (data) {
            referenceSelect.select2('destroy').empty();
            $.each(data.obj, function (i, item) {
                referenceSelect.append($('<option>').attr('value', item.key).text(item.value));
            });
            referenceSelect.select2({ dropdownParent: referenceSelect.parent() });
        },
        error: (xhr, status, err) => hsServerError(xhr, status, err)
    });
}

/**
 * S2HSHelper — reusable Select2-over-AJAX module.
 * Supports preselect (edit mode), clear, setValue, getValue.
 * FIX: added optional `templateConfig` for custom result/selection rendering.
 */
window.S2HSHelper = (function () {

    /**
     * Initialise a select2 with AJAX.
     *
     * @param {string}   selector       CSS selector
     * @param {string}   url            AJAX search endpoint
     * @param {string}   placeholder
     * @param {*}        [preId]        Preselect value (edit mode)
     * @param {string}   [preText]      Preselect display text
     * @param {object}   [extraParams]  Extra static query params
     * @param {object}   [templateConfig]  Optional { result(item), selection(item) } functions
     */
    function init(selector, url, placeholder, preId, preText, extraParams = {}, templateConfig = null) {
        const $el = $(selector);
        if ($el.hasClass('select2-hidden-accessible')) $el.select2('destroy');

        const s2Options = {
            dropdownParent:     _getDropdownParent($el),
            width:              '100%',
            placeholder:        placeholder || 'Select option',
            allowClear:         true,
            minimumInputLength: 0,
            ajax: {
                url,
                dataType: 'json',
                delay:    250,
                data: function (params) {
                    return { search: params.term || '', page: params.page || 1, ...extraParams };
                },
                processResults: function (data) {
                    return {
                        results:    (data.items || []).map(item => ({ id: item.id, text: item.text, ...item })),
                        pagination: { more: !!data.hasMore }
                    };
                },
                cache: true
            }
        };

        // FIX: support custom templateResult / templateSelection
        if (templateConfig) {
            if (typeof templateConfig.result    === 'function') s2Options.templateResult    = templateConfig.result;
            if (typeof templateConfig.selection === 'function') s2Options.templateSelection = templateConfig.selection;
        }

        $el.select2(s2Options);

        if (preId) {
            $el.append(new Option(preText || preId, preId, true, true)).trigger('change');
        }
    }

    function _getDropdownParent($el) {
        const modal = $el.closest('.modal');
        return modal.length ? modal : $(document.body);
    }

    function clear(selector)              { $(selector).val(null).trigger('change'); }
    function setValue(selector, id, text) { $(selector).append(new Option(text, id, true, true)).trigger('change'); }
    function getValue(selector)           { return $(selector).val(); }

    return { init, clear, setValue, getValue };
})();


/* ═══════════════════════════════════════════════════════════════════════════
   §10  TABLE RENDER HELPERS  (legacy template-based row builders)
        These remain for backward-compat with older pages.
        New pages should use DataTable server-side rendering instead.
   ═══════════════════════════════════════════════════════════════════════════ */

function hs_add_table_data(fields, properties, prefix, detailsName, tableId, footerEnable = false) {
    $('#' + tableId + ' > tbody').html('');
    var rowNumber = 1;
    $.each(properties, function (key, value) {
        var remainingFields = '';
        var tableRow = '<tr><td>' + (++key) + '</td>';
        value['sort_order'] = rowNumber;
        $.each(fields, function (keys, values) {
            var hsSplitData = values.split('__');
            if (hsSplitData[1].includes('multiply')) {
                var parts = hsSplitData[1].split('*');
                value[hsSplitData[0]] = hsFloatConverter(value[parts[1]]) * hsFloatConverter(value[parts[2]]);
            }
            remainingFields += '<input type="hidden" class="' + keys + '" name="' + detailsName + '[' + rowNumber + '].' + keys + '" value="' + nullCheck(value[hsSplitData[0]]) + '"/>';
            if (hsSplitData[1].includes('table')) {
                tableRow += '<td>' + nullCheck(value[hsSplitData[0]]) + '</td>';
            }
        });
        tableRow += '<td ref_id="' + rowNumber + '">' + remainingFields +
            '<a href="javascript:;" onclick="hs_' + prefix + '_' + detailsName + 'EditEvent(this)" ref_id="' + rowNumber + '" class="btn btn-white btn-sm"><i class="fa-regular fa-pen-to-square"></i></a>' +
            '<a href="javascript:;" onclick="hs_' + prefix + '_' + detailsName + 'DeleteEvent(this)" ref_id="' + rowNumber + '" class="btn btn-white btn-sm"><i class="fa-regular text-danger fa-trash-can"></i></a>' +
            '</td></tr>';
        rowNumber++;
        $('#' + tableId + ' > tbody').prepend(tableRow);
    });
    if (footerEnable) hs_add_table_dataFooter(fields, properties, prefix, detailsName, tableId);
}

function hs_divide_table_data(fields, properties, prefix, detailsName, tableId, footerEnable = false) {
    $('#' + tableId + ' > tbody').html('');
    var rowNumber = 1;
    $.each(properties, function (key, value) {
        var remainingFields = '';
        var tableRow = '<tr><td>' + (++key) + '</td>';
        value['sort_order'] = rowNumber;
        $.each(fields, function (keys, values) {
            var hsSplitData = values.split('__');
            if (hsSplitData[1].includes('multiply')) {
                var parts = hsSplitData[1].split('*');
                value[hsSplitData[0]] = hsFloatConverter(value[parts[1]]) * hsFloatConverter(value[parts[2]]);
            }
            remainingFields += '<input type="hidden" class="' + keys + '" name="' + detailsName + '[' + rowNumber + '].' + keys + '" value="' + nullCheck(value[hsSplitData[0]]) + '"/>';
            if (hsSplitData[1].includes('table')) {
                tableRow += '<td>' + nullCheck(value[hsSplitData[0]]) + '</td>';
            }
        });
        tableRow += '<td ref_id="' + rowNumber + '">' + remainingFields +
            '<a href="javascript:;" onclick="hs_' + prefix + '_' + detailsName + 'DividedEvent(this)" ref_id="' + rowNumber + '" class="btn btn-white btn-sm"><i class="fa-solid fa-divide"></i></a>' +
            '</td></tr>';
        rowNumber++;
        $('#' + tableId + ' > tbody').prepend(tableRow);
    });
    if (footerEnable) hs_add_table_dataFooter(fields, properties, prefix, detailsName, tableId);
}

function hs_add_table_dataFooter(fields, properties, prefix, detailsName, tableId) {
    const objectMap = {};
    $.each(fields, function (key, value) {
        $.each(properties, function (keys, values) {
            if (value.includes('table')) {
                objectMap[key] = value.includes('sumFooter')
                    ? hsFloatConverter(objectMap[key]) + hsFloatConverter(values[key])
                    : '-';
            }
        });
    });
    var tableRows = '<tr><td>SUM</td>';
    $.each(objectMap, function (key, value) { tableRows += '<td>' + value + '</td>'; });
    tableRows += '<td>-</td></tr>';
    $('#' + tableId + ' > tbody').append(tableRows);
}

function hs_add_table_inner_data(fields, properties, prefix, detailsName, tableId, extraAction) {
    $('#' + tableId + ' > tbody').html('');
    var hsTableRowNumber = 1;
    $.each(properties, function (key, value) {
        var remainingFields = '';
        var tableRow = '<tr><td>' + (++key) + '</td>';
        value['sort_order'] = hsTableRowNumber;
        $.each(fields, function (keys, values) {
            if (keys === 'dtlLine') {
                tableRow += hsInnerTablesHtml(prefix, hsTableRowNumber, keys, value, values, detailsName);
            } else {
                var hsSplitData = values.split('__');
                if (hsSplitData[1].includes('multiply')) {
                    var parts = hsSplitData[1].split('*');
                    value[hsSplitData[0]] = hsFloatConverter(value[parts[1]]) * hsFloatConverter(value[parts[2]]);
                }
                remainingFields += '<input type="hidden" class="' + keys + '" name="' + detailsName + '[' + hsTableRowNumber + '].' + keys + '" value="' + nullCheck(value[hsSplitData[0]]) + '"/>';
                if (hsSplitData[1].includes('table')) {
                    tableRow += '<td>' + nullCheck(value[hsSplitData[0]]) + '</td>';
                }
            }
        });
        tableRow += '<td ref_id="' + hsTableRowNumber + '">' + remainingFields +
            '<a href="javascript:;" onclick="hs_' + prefix + '_' + detailsName + 'AddEvent(this)"    ref_id="' + hsTableRowNumber + '" class="btn btn-white btn-sm"><i class="fa-regular fa-square-plus text-success"></i></a>' +
            '<a href="javascript:;" onclick="hs_' + prefix + '_' + detailsName + 'EditEvent(this)"   ref_id="' + hsTableRowNumber + '" class="btn btn-white btn-sm"><i class="fa-regular fa-pen-to-square"></i></a>' +
            '<a href="javascript:;" onclick="hs_' + prefix + '_' + detailsName + 'DeleteEvent(this)" ref_id="' + hsTableRowNumber + '" class="btn btn-white btn-sm"><i class="fa-regular text-danger fa-trash-can"></i></a>';
        $.each(extraAction, function (k, v) {
            if (v) tableRow += '<a href="javascript:;" onclick="hs_' + prefix + '_' + detailsName + 'ExtraEvent' + k + '(this)" ref_id="' + hsTableRowNumber + '" class="btn btn-white btn-sm"><i class="fa-regular text-success ' + v + '"></i></a>';
        });
        tableRow += '</td></tr>';
        hsTableRowNumber++;
        $('#' + tableId + ' > tbody').prepend(tableRow);
    });
}

function hsInnerTablesHtml(prefix, hsTableRowNumber, keys, value, values, detailsName) {
    var tableRowInner = "<td><table class='table table-bordered table-striped table-hover' materTableRef='" + hsTableRowNumber + "'>";
    tableRowInner += '<thead><tr>';
    $.each(values, function (keyInn, valueInn) {
        if (keyInn !== 'id' && keyInn !== 'sortOrder' && valueInn.includes('table')) {
            tableRowInner += '<td>' + valueInn.split(':::')[1] + '</td>';
        }
    });
    tableRowInner += '<td>Action</td></tr></thead>';
    var hsTableRowNumberLine = 1;
    $.each(value[keys], function (keyInnVal, valueInnVal) {
        tableRowInner += '<tr>';
        var remainingLineFields = '';
        valueInnVal['sort_order'] = hsTableRowNumberLine;
        $.each(values, function (keyInn, valueInn) {
            remainingLineFields += '<input type="hidden" class="' + keyInn + '" name="' + detailsName + '[' + hsTableRowNumber + '].' + keys + '[' + hsTableRowNumberLine + '].' + keyInn + '" value="' + nullCheck(valueInnVal[valueInn.split('__')[0]]) + '"/>';
            if (keyInn !== 'id' && keyInn !== 'sortOrder' && valueInn.includes('table')) {
                tableRowInner += '<td>' + nullCheck(valueInnVal[valueInn.split('__')[0]]) + '</td>';
            }
        });
        tableRowInner += '<td ref_id="' + hsTableRowNumber + '" ref_line_id="' + hsTableRowNumberLine + '">' + remainingLineFields +
            '<a href="javascript:;" onclick="hs_' + prefix + '_' + detailsName + '_' + keys + 'EditEvent(this)"   ref_id="' + hsTableRowNumber + '" ref_line_id="' + hsTableRowNumberLine + '" class="btn btn-white btn-sm"><i class="fa-regular fa-pen-to-square"></i></a>' +
            '<a href="javascript:;" onclick="hs_' + prefix + '_' + detailsName + '_' + keys + 'DeleteEvent(this)" ref_id="' + hsTableRowNumber + '" ref_line_id="' + hsTableRowNumberLine + '" class="btn btn-white btn-sm"><i class="fa-regular text-danger fa-trash-can"></i></a>' +
            '</td></tr>';
        hsTableRowNumberLine++;
    });
    tableRowInner += '</table></td>';
    return tableRowInner;
}

function hs_add_table_inner_data_new(fields, properties, prefix, detailsName, tableId, extraAction, detailCreateFalse, detailCloneTrue) {
    $('#' + tableId + ' > tbody').html('');
    var hsTableRowNumber = 1;
    $.each(properties, function (key, value) {
        var remainingFields = '<input type="hidden" class="id" name="' + detailsName + '[' + hsTableRowNumber + '].id" value="' + nullCheck(value['id']) + '"/>' +
                              '<input type="hidden" class="sortOrder" name="' + detailsName + '[' + hsTableRowNumber + '].sortOrder" value="' + hsTableRowNumber + '"/>';
        var tableRow = '<tr><td>' + (++key) + '</td>';
        value['sort_order'] = hsTableRowNumber;
        $.each(fields, function (keys, values) {
            if (keys === 'dtlLine') {
                tableRow += hsInnerTablesHtmlNew(hsTableRowNumber, prefix, detailsName, keys, value.dtlLine, values);
            } else {
                var hsSplitData = values.split('__');
                if (hsSplitData[1].includes('multiply')) {
                    var parts = hsSplitData[1].split('*');
                    value[hsSplitData[0]] = hsFloatConverter(value[parts[1]]) * hsFloatConverter(value[parts[2]]);
                }
                remainingFields += '<input type="hidden" class="' + keys + '" name="' + detailsName + '[' + hsTableRowNumber + '].' + keys + '" value="' + nullCheck(value[hsSplitData[0]]) + '"/>';
                if (hsSplitData[1].includes('table')) {
                    tableRow += hsSplitData[1].includes('function')
                        ? '<td>(' + keys + '_' + hsSplitData[1].split('__')[1] + ')</td>'
                        : '<td>' + nullCheck(value[hsSplitData[0]]) + '</td>';
                }
            }
        });
        tableRow += '<td ref_id="' + hsTableRowNumber + '">' + remainingFields +
            (!detailCreateFalse && !isItNull(fields.dtlLine) ? '<a href="javascript:;" onclick="hs_' + prefix + '_' + detailsName + 'AddEvent(this)"    ref_id="' + hsTableRowNumber + '" class="btn btn-white btn-sm"><i class="fa-regular fa-square-plus text-success"></i></a>' : '') +
            (!detailCreateFalse                              ? '<a href="javascript:;" onclick="hs_' + prefix + '_' + detailsName + 'EditEvent(this)"   ref_id="' + hsTableRowNumber + '" class="btn btn-white btn-sm"><i class="fa-regular fa-pen-to-square"></i></a>'               : '') +
            (detailCloneTrue                                 ? '<a href="javascript:;" onclick="hs_' + prefix + '_' + detailsName + 'CloneEvent(this)"  ref_id="' + hsTableRowNumber + '" class="btn btn-white btn-sm"><i class="fa-regular fa-clone"></i></a>'                      : '') +
            '<a href="javascript:;" onclick="hs_' + prefix + '_' + detailsName + 'DeleteEvent(this)" ref_id="' + hsTableRowNumber + '" class="btn btn-white btn-sm"><i class="fa-regular text-danger fa-trash-can"></i></a>';
        $.each(extraAction, function (k, v) {
            if (v) tableRow += '<a href="javascript:;" onclick="hs_' + prefix + '_' + detailsName + 'ExtraEvent' + k + '(this)" ref_id="' + hsTableRowNumber + '" class="btn btn-white btn-sm"><i class="fa-regular text-success ' + v + '"></i></a>';
        });
        tableRow += '</td></tr>';
        hsTableRowNumber++;
        $('#' + tableId + ' > tbody').prepend(tableRow);
    });
}

function hsInnerTablesHtmlNew(hsTableRowNumber, prefix, detailsName, detailsNameLine, values, properties) {
    var tableRowInner = "<td><table class='table table-bordered table-striped table-hover' materTableRef='" + hsTableRowNumber + "'>";
    tableRowInner += '<thead><tr>';
    var fileRow = '';
    $.each(properties, function (keyInn, valueInn) {
        if (keyInn !== 'id' && keyInn !== 'sortOrder' && valueInn.includes('table')) {
            tableRowInner += '<td>' + nullCheck(valueInn.split(':::')[1]) + '</td>';
            if (valueInn.includes('fileUploadShow') && valueInn.includes('fileUploadInput')) {
                fileRow = nullCheck(valueInn.split(':::')[0]);
            }
        }
    });
    tableRowInner += '<td>Action</td></tr></thead>';
    var hsTableRowNumberLine = 1;
    $.each(values, function (keyInnVal, valueInnVal) {
        tableRowInner += '<tr>';
        var remainingLineFields = '<input type="hidden" class="id" name="' + detailsName + '[' + hsTableRowNumber + '].' + detailsNameLine + '[' + hsTableRowNumberLine + '].id" value="' + nullCheck(valueInnVal['id']) + '"/>' +
                                  '<input type="hidden" class="sortOrder" name="' + detailsName + '[' + hsTableRowNumber + '].' + detailsNameLine + '[' + hsTableRowNumberLine + '].sortOrder" value="' + hsTableRowNumberLine + '"/>';
        valueInnVal['sort_order'] = hsTableRowNumberLine;
        $.each(properties, function (keyInn, valueInn) {
            remainingLineFields += '<input type="hidden" class="' + keyInn + '" name="' + detailsName + '[' + hsTableRowNumber + '].' + detailsNameLine + '[' + hsTableRowNumberLine + '].' + keyInn + '" value="' + nullCheck(valueInnVal[valueInn.split('__')[0]]) + '"/>';
            if (keyInn !== 'id' && keyInn !== 'sortOrder' && valueInn.includes('table')) {
                if (valueInn.includes('fileUploadShow')) {
                    if (valueInn.includes('fileUploadInput')) {
                        tableRowInner += isItNull(fileRow) ? '' : '<td><input style="width:100px;" type="file" class="' + fileRow + '" name="' + detailsName + '[' + hsTableRowNumber + '].' + detailsNameLine + '[' + hsTableRowNumberLine + '].' + keyInn + '"/> &nbsp;' + (isItNull(valueInnVal[valueInn.split('__')[0]]) ? '' : '<a href="javascript:;" onclick="' + keyInn + 'FileDownloadEvent(' + nullCheck(valueInnVal['id']) + ')"><i class="fa fa-cloud-download text-danger"></i></a>') + '</td>';
                    } else {
                        tableRowInner += '<td><a href="javascript:;" onclick="' + keyInn + 'FileDownloadEvent(' + nullCheck(valueInnVal['id']) + ')"><i class="fa fa-cloud-download text-danger"></i></a></td>';
                    }
                } else {
                    tableRowInner += '<td>' + nullCheck(valueInnVal[valueInn.split('__')[0]]) + '</td>';
                }
            }
        });
        tableRowInner += '<td ref_id="' + hsTableRowNumber + '" ref_line_id="' + hsTableRowNumberLine + '">' + remainingLineFields +
            '<a href="javascript:;" onclick="hs_' + prefix + '_' + detailsName + '_' + detailsNameLine + 'EditEvent(this)"   ref_id="' + hsTableRowNumber + '" ref_line_id="' + hsTableRowNumberLine + '" class="btn btn-white btn-sm"><i class="fa-regular fa-pen-to-square"></i></a>' +
            '<a href="javascript:;" onclick="hs_' + prefix + '_' + detailsName + '_' + detailsNameLine + 'DeleteEvent(this)" ref_id="' + hsTableRowNumber + '" ref_line_id="' + hsTableRowNumberLine + '" class="btn btn-white btn-sm"><i class="fa-regular text-danger fa-trash-can"></i></a>' +
            '</td></tr>';
        hsTableRowNumberLine++;
    });
    tableRowInner += '</table></td>';
    return tableRowInner;
}

function hs_view_table_data(fields, dataObj, prefix, detailsName, tableId, extraAction, footerEnable = false) {
    $('#' + tableId + ' > tbody').html('');
    var rowNumber = 1;
    $.each(dataObj, function (key, value) {
        value['sort_order'] = rowNumber;
        var hsAttributesHtml = '';
        var tableRowBody     = '';
        $.each(fields, function (keys, values) {
            if (typeof values === 'object') {
                var tableRowInner = "<table class='table table-bordered table-striped table-hover'><thead><tr>";
                $.each(values, function (keyInn, valueInn) {
                    if (keyInn !== 'id' && keyInn !== 'sortOrder' && valueInn.includes('table')) {
                        tableRowInner += '<td>' + nullCheck(valueInn.split(':::')[1]) + '</td>';
                    }
                });
                tableRowInner += '<td>Action</td></tr></thead>';
                $.each(value[keys], function (keyInnVal, valueInnVal) {
                    var rowNumberLine = 1;
                    tableRowInner += '<tr>';
                    var hsAttributesHtmlLine = '';
                    $.each(values, function (keyInn, valueInn) {
                        hsAttributesHtmlLine += keyInn + '="' + nullCheck(valueInnVal[valueInn.split('__')[0]]) + '" ';
                        if (keyInn !== 'id' && keyInn !== 'sortOrder' && valueInn.includes('table')) {
                            tableRowInner += '<td>' + nullCheck(valueInnVal[valueInn.split('__')[0]]) + '</td>';
                        }
                    });
                    tableRowInner += '<td>' +
                        '<a href="javascript:;" ' + hsAttributesHtmlLine + ' onclick="hs_' + prefix + '_' + detailsName + '_' + keys + 'ShowEvent(this)" ref_id="' + rowNumber + '" ref_line_id="' + rowNumberLine + '" class="btn btn-white btn-sm"><i class="fa-regular fas fa-book-open-reader text-success"></i></a>' +
                        '</td></tr>';
                    rowNumberLine++;
                });
                tableRowInner += '</table>';
                tableRowBody += '<td>' + tableRowInner + '</td>';
            } else {
                var hsSplitData = values.split('__');
                if (hsSplitData[1].includes('table')) {
                    tableRowBody += '<td>' + nullCheck(value[hsSplitData[0]]) + '</td>';
                }
                hsAttributesHtml += keys + '="' + nullCheck(value[hsSplitData[0]]) + '" ';
            }
            rowNumber++;
        });
        var tableRow = '<tr><td>' + (++key) + '</td>' + tableRowBody;
        $.each(extraAction, function (k, v) {
            if (v) tableRow += '<a href="javascript:;" onclick="hs_' + tableId + 'ShowEvent' + k + '(' + value['id'] + ')" class="btn btn-white btn-sm"><i class="fa-regular text-success ' + v + '"></i></a>';
        });
        tableRow += '</tr>';
        $('#' + tableId + ' > tbody').append(tableRow);
        rowNumber++;
        if (footerEnable) hs_add_table_dataFooter(fields, dataObj, prefix, detailsName, tableId);
    });
}

function hs_view_table_dataNew(properties, dataObj, prefix, detailsName, tableId, extraAction) {
    $('#' + tableId + ' > tbody').html('');
    var rowNumberDetail = 1;
    $.each(dataObj, function (key, value) {
        var hsAttributesHtml = '';
        var tableRowBody     = '';
        $.each(properties, function (keys, values) {
            if (keys === 'dtlLine') {
                var tableRowInner = "<table class='table table-bordered table-striped table-hover'><thead><tr>";
                $.each(values, function (keyInn, valueInn) {
                    if (keyInn !== 'id' && keyInn !== 'sortOrder' && valueInn.includes('table')) {
                        tableRowInner += '<td>' + nullCheck(valueInn.split(':::')[1]) + '</td>';
                    }
                });
                tableRowInner += '<td>Action</td></tr></thead>';
                $.each(value.dtlLine, function (keyInnVal, valueInnVal) {
                    var rowNumberLine    = 1;
                    var hsAttributesHtmlLine = '';
                    tableRowInner += '<tr>';
                    $.each(values, function (keyInn, valueInn) {
                        hsAttributesHtmlLine += keyInn + '="' + nullCheck(valueInnVal[valueInn.split('__')[0]]) + '" ';
                        if (keyInn !== 'id' && keyInn !== 'sortOrder' && valueInn.includes('table')) {
                            if (valueInn.includes('fileUploadShow')) {
                                tableRowInner += isItNull(valueInnVal[valueInn.split('__')[0]])
                                    ? '<td>-</td>'
                                    : '<td><a href="javascript:;" onclick="' + keyInn + 'FileDownloadEvent(' + nullCheck(valueInnVal['id']) + ')"><i class="fa fa-cloud-download text-danger"></i></a></td>';
                            } else if (valueInn.includes('customCaption')) {
                                tableRowInner += '<td>' + nullCheck(valueInnVal[valueInn.split('__')[0] + '_custom_caption']) + '</td>';
                            } else {
                                tableRowInner += '<td>' + nullCheck(valueInnVal[valueInn.split('__')[0]]) + '</td>';
                            }
                        }
                    });
                    tableRowInner += '<td ref_id="' + rowNumberDetail + '" ref_line_id="' + rowNumberLine + '">' +
                        '<a href="javascript:;" ' + hsAttributesHtmlLine + ' onclick="hs_' + prefix + '_' + detailsName + '_dtlLineViewTableEvent(this)" ref_id="' + rowNumberDetail + '" ref_line_id="' + rowNumberLine + '" class="btn btn-white btn-sm"><i class="fa-regular fas fa-book-open-reader text-success"></i></a>' +
                        '</td></tr>';
                    rowNumberLine++;
                });
                tableRowInner += '</table>';
                tableRowBody += '<td>' + tableRowInner + '</td>';
            } else {
                var hsSplitData = values.split('__');
                if (hsSplitData[1].includes('table')) {
                    tableRowBody += hsSplitData[1].includes('customCaption')
                        ? '<td>' + nullCheck(value[hsSplitData[0] + '_custom_caption']) + '</td>'
                        : '<td>' + nullCheck(value[hsSplitData[0]]) + '</td>';
                }
                hsAttributesHtml += keys + '="' + nullCheck(value[hsSplitData[0]]) + '" ';
            }
        });
        var tableRow = '<tr><td>' + (++key) + '</td>' + tableRowBody + '<td>' +
            '<a href="javascript:;" onclick="hs_' + tableId + 'Event(this)" ref_id="' + rowNumberDetail + '" class="btn btn-white btn-sm"><i class="fa-regular fas fa-book-open-reader text-success"></i></a>';
        $.each(extraAction, function (k, v) {
            if (v) tableRow += '<a href="javascript:;" onclick="hs_' + tableId + 'ShowEvent' + k + '(' + value['id'] + ')" ref_id="' + rowNumberDetail + '" class="btn btn-white btn-sm"><i class="fa-regular text-success ' + v + '"></i></a>';
        });
        tableRow += '</td></tr>';
        $('#' + tableId + ' > tbody').append(tableRow);
        rowNumberDetail++;
    });
}


/* ═══════════════════════════════════════════════════════════════════════════
   §11  CURRENCY / DATE UTILITIES
   ═══════════════════════════════════════════════════════════════════════════ */

/**
 * Format a numeric value as currency.
 * FIX: removed two stray console.log() calls from production code.
 *
 * @param  {number|string} value
 * @param  {string}        [currency='BDT']
 * @returns {string}
 */
function formatCurrency(value, currency = 'BDT') {
    // FIX: console.log(value) removed
    if (value === null || value === undefined || value === '' || isNaN(value)) return '0.00';
    const num = parseFloat(value.toString().replace(/,/g, ''));
    // FIX: console.log(num) removed
    return new Intl.NumberFormat('en-BD', {
        style:                'currency',
        currency,
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(num);
}

/**
 * Parse a float safely — returns 0 for null/empty/invalid.
 * @param  {*} value
 * @returns {number}
 */
function hsFloatConverter(value) {
    if (value == null || value === '' || value === 'null' || value === 'NULL') return 0;
    const n = parseFloat(value);
    return isNaN(n) ? 0 : n;
}

/**
 * Convert between dd-MM-yyyy and yyyy-MM-dd date formats.
 *
 * @param  {string} date         Input date string
 * @param  {'toInput'|'toDisplay'} [direction='toInput']
 *         'toInput'   — dd-MM-yyyy  → yyyy-MM-dd  (for <input type="date">)
 *         'toDisplay' — yyyy-MM-dd  → dd-MM-yyyy  (for display spans)
 * @returns {string}  Converted date or '' if invalid
 */
function hsFormatDate(date, direction = 'toInput') {
    if (!date || date === '—') return '';
    const p = date.split('-');
    if (p.length !== 3) return '';
    return direction === 'toInput'
        ? `${p[2]}-${p[1]}-${p[0]}`   // dd-MM-yyyy → yyyy-MM-dd
        : `${p[2]}-${p[1]}-${p[0]}`;  // yyyy-MM-dd → dd-MM-yyyy (same logic, symmetric)
}

/**
 * Calculate calendar-day difference between two dd-MM-yyyy strings.
 * @param  {string} first   dd-MM-yyyy
 * @param  {string} second  dd-MM-yyyy
 * @returns {number|string}
 */
function dateDiffByPicker(first, second) {
    if (isItNull(first) || isItNull(second)) return '';
    return Math.round(
        (moment(second, 'DD-MM-YYYY') - moment(first, 'DD-MM-YYYY')) / (1000 * 60 * 60 * 24)
    );
}

/**
 * Get today's date as dd-MM-yyyy string.
 * @returns {string}
 */
function asgSetDateToPicker() {
    const d  = new Date();
    const dd = String(d.getDate()).padStart(2, '0');
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    return `${dd}-${mm}-${d.getFullYear()}`;
}


/* ═══════════════════════════════════════════════════════════════════════════
   §12  DOM UTILITIES
   ═══════════════════════════════════════════════════════════════════════════ */

/**
 * Return a safe string — replaces null / undefined / 'null' with ''.
 * @param  {*} checkvalue
 * @returns {string}
 */
function nullCheck(checkvalue) {
    return (checkvalue == null || checkvalue === 'null' || checkvalue === undefined)
        ? ''
        : checkvalue;
}

/**
 * Return true if the value is null / undefined / empty / 'null'.
 * @param  {*} check_value
 * @returns {boolean}
 */
function isItNull(check_value) {
    return (check_value == null || check_value === undefined ||
            check_value === 'null' || check_value === '');
}

/**
 * Safely set textContent of an element by ID.
 * @param {string} id
 * @param {string} text
 */
function hsSetText(id, text) {
    const el = document.getElementById(id);
    if (el) el.textContent = text ?? '';
}

/**
 * Safely set innerHTML of an element by ID.
 * Use only for trusted/sanitised HTML (e.g. badge markup from server).
 * @param {string} id
 * @param {string} html
 */
function hsSetHtml(id, html) {
    const el = document.getElementById(id);
    if (el) el.innerHTML = html ?? '';
}


/* ═══════════════════════════════════════════════════════════════════════════
   §13  IMPORT / EXPORT / DROPDOWN HELPERS
   ═══════════════════════════════════════════════════════════════════════════ */

/**
 * Trigger a data import with a loading dialog.
 *
 * @param {string}               label       Human-readable entity name
 * @param {string}               endpoint    POST URL
 * @param {DataTable|HTMLElement|null} [dataTable]
 * @param {object|null}          [payload]   Override default { url: label }
 */
function importData(label, endpoint, dataTable = null, payload = null) {
    const requestData = payload || { url: label.toLowerCase() };

    Swal.fire({
        title:             `Importing ${label}…`,
        text:              `Please wait while ${label.toLowerCase()} data is being imported.`,
        allowOutsideClick: false,
        didOpen:           () => Swal.showLoading()
    });

    secureFetch(endpoint, { method: 'POST', body: JSON.stringify(requestData) })
        .then(data => {
            Swal.close();
            if (data.success) {
                Swal.fire({
                    icon:  'success',
                    title: `${label} Import Completed`,
                    html:  `<p>${data.message || ''}</p>
                            <p><b>Successful:</b> ${data.successCount || 0}</p>
                            <p><b>Failed:</b> ${data.failedCount || 0}</p>`,
                    timer: 5000
                });
                if (dataTable) _reloadDataTable(dataTable);
            } else {
                Swal.fire({ icon: 'error', title: `${label} Import Failed`, text: data.message || 'Unknown error.' });
            }
        })
        .catch(err => {
            Swal.close();
            Swal.fire({ icon: 'error', title: `${label} Import Error`, text: err.message });
        });
}

/**
 * Populate a <select> element from a URL or a pre-fetched data array.
 * Each item should have `{ value, display }` shape.
 *
 * @param {object} opts
 * @param {string}  [opts.url]           Fetch URL (ignored if `data` provided)
 * @param {Array}   [opts.data]          Pre-fetched items array
 * @param {string}  opts.elementId       Target <select> element ID
 * @param {*}       [opts.selectedValue] Pre-select this value after population
 */
function asgLoadDropdown({ url = null, data = null, elementId, selectedValue = null }) {
    const populate = (items) => {
        const dropdown   = document.getElementById(elementId);
        const firstOption = dropdown.querySelector('option');
        dropdown.innerHTML = '';
        if (firstOption) dropdown.appendChild(firstOption);
        items.forEach(item => {
            const option       = new Option(item.display, item.value);
            option.selected    = selectedValue != null && item.value === selectedValue;
            dropdown.appendChild(option);
        });
    };

    if (data)       { populate(data); return; }
    if (url)        { secureFetch(url).then(populate).catch(err => console.error(`[asgLoadDropdown] ${elementId}:`, err)); }
}

/**
 * Populate a <select> element from a URL.
 * FIX: renamed from `haPopulateSelect` (typo) — backward-compat alias kept below.
 *
 * @param {string}   url
 * @param {string}   selectId
 * @param {Function} optionTextBuilder  Receives each item, returns display string
 */
function hsPopulateSelect(url, selectId, optionTextBuilder) {
    secureFetch(url)
        .then(data => {
            const select = document.getElementById(selectId);
            if (!select) return;
            select.innerHTML = '';
            data.forEach(item => {
                select.appendChild(new Option(optionTextBuilder(item), item.id));
            });
        })
        .catch(err => console.error('[hsPopulateSelect]', err));
}

/** @deprecated  Use hsPopulateSelect() instead. */
const haPopulateSelect = hsPopulateSelect;


/* ═══════════════════════════════════════════════════════════════════════════
   §14  ACTION HANDLER MODULE
   ═══════════════════════════════════════════════════════════════════════════ */

window.ActionHandler = (function () {
    let cfg = {};

    function init(userCfg) { cfg = userCfg; }

    function handle(id, action) {
        const actionCfg = cfg.actions && cfg.actions[action];
        if (!actionCfg) { console.warn('[ActionHandler] Unknown action:', action); return; }

        const urlBase = `${cfg.baseUrl}/${id}`;

        if (actionCfg.type === 'fetch') {
            return secureFetch(urlBase).then(res => {
                if (!res.success) return Swal.fire('Error', res.message, 'error');
                if (actionCfg.modal === 'view') {
                    if (typeof populateDocView === 'function') populateDocView(res.data);
                    hsOpenModal('defaultModalView');
                } else if (actionCfg.modal === 'edit') {
                    if (typeof populateDocForm === 'function') populateDocForm(res.data);
                    hsOpenModal('defaultModal');
                }
            });
        }

        if (actionCfg.type === 'delete') {
            return confirmAndExecute({
                title:        'Delete?',
                text:         'This cannot be undone.',
                icon:         'warning',
                confirmText:  'Delete',
                confirmColor: '#ef4444',
                url:          urlBase,
                method:       'DELETE',
                successTitle: 'Deleted!',
                reloadTable:  true,
                dataTable:    cfg.table
            });
        }

        if (actionCfg.type === 'dialog') {
            return approvalActionDialog({
                title:              actionCfg.title,
                icon:               'question',
                confirmText:        actionCfg.confirmText,
                confirmColor:       actionCfg.confirmColor,
                textareaLabel:      '',
                textareaPlaceholder: actionCfg.textareaPlaceholder,
                textareaRequired:   actionCfg.textareaRequired,
                endpoint:           `${urlBase}/${action}`,
                successTitle:       actionCfg.successTitle,
                tableToReload:      cfg.table
            });
        }
    }

    return { init, handle };
})();


/* ═══════════════════════════════════════════════════════════════════════════
   §15  ADVANCED SUBMIT HELPERS
   ═══════════════════════════════════════════════════════════════════════════ */

/**
 * Submit a payload via secureFetch with button-state management.
 * FIX: removed hardcoded "Change Password" in finally block.
 *      `defaultBtnText` param controls the restored label (default: "Save").
 *
 * @param {object} opts
 * @param {string}       opts.url
 * @param {string}       [opts.method='POST']
 * @param {object}       opts.body
 * @param {HTMLElement}  [opts.submitBtn]
 * @param {HTMLElement}  [opts.spinner]
 * @param {HTMLElement}  [opts.btnText]
 * @param {string}       [opts.loadingText='Processing…']
 * @param {string}       [opts.defaultBtnText='Save']     FIX: was hardcoded "Change Password"
 * @param {string}       [opts.successTitle='Success!']
 * @param {string|null}  [opts.successMessage]
 * @param {Function|null} [opts.onSuccess]
 * @param {Function|null} [opts.onFinally]
 */
window.submitWithSecureFetch = function submitWithSecureFetch({
    url,
    method         = 'POST',
    body,
    submitBtn,
    spinner,
    btnText,
    loadingText    = 'Processing…',
    defaultBtnText = 'Save',           // FIX: was hardcoded "Change Password"
    successTitle   = 'Success!',
    successMessage = null,
    onSuccess      = null,
    onFinally      = null
}) {
    if (submitBtn) submitBtn.disabled = true;
    if (spinner)   spinner.classList.remove('d-none');
    if (btnText)   btnText.textContent = loadingText;

    secureFetch(url, { method, body: JSON.stringify(body) })
        .then(data => {
            if (!data.success) throw new Error(data.message || 'Operation failed');
            Swal.fire({
                icon:              'success',
                title:             successTitle,
                text:              successMessage || data.message,
                timer:             2000,
                showConfirmButton: false
            });
            if (typeof onSuccess === 'function') onSuccess(data);
        })
        .catch(err => {
            Swal.fire({ icon: 'error', title: 'Error!', text: err.message });
        })
        .finally(() => {
            if (submitBtn) submitBtn.disabled = false;
            if (spinner)   spinner.classList.add('d-none');
            if (btnText)   btnText.textContent = defaultBtnText; // FIX
            if (typeof onFinally === 'function') onFinally();
        });
};

/**
 * Attach a Parsley-validated submit listener to a form with full lifecycle management.
 * FIX: null-guarded DataTable reload; cleaner error propagation.
 *
 * @param {string}       formId
 * @param {string}       submitBtnId
 * @param {string}       spinnerId
 * @param {string}       btnTextId
 * @param {string}       saveUrl
 * @param {string|null}  [reloadTableSelector]
 * @param {Function|null} [modifyDataCallback]     Receives objectified form data, may return modified version
 */
function hsInitAjaxForm(
    formId,
    submitBtnId,
    spinnerId,
    btnTextId,
    saveUrl,
    reloadTableSelector = null,
    modifyDataCallback  = null
) {
    const form      = document.querySelector(formId);
    const submitBtn = document.querySelector(submitBtnId);
    const spinner   = document.querySelector(spinnerId);
    const btnText   = document.querySelector(btnTextId);

    if (!form) { console.warn(`[hsInitAjaxForm] Form "${formId}" not found.`); return; }

    $(form).parsley();

    form.addEventListener('submit', function (e) {
        e.preventDefault();
        if (!$(form).parsley().validate()) return;

        hsDisableButton(submitBtn, spinner, btnText);

        let payload = objectifyForm(form);
        if (typeof modifyDataCallback === 'function') {
            payload = modifyDataCallback(payload) || payload;
        }

        secureFetch(saveUrl, { method: 'POST', body: JSON.stringify(payload) })
            .then(data => {
                // FIX: null guard — only reload if a valid selector was provided
                const dt = reloadTableSelector ? $(reloadTableSelector).DataTable() : null;
                hsAfterSaveMessages(data.message, data.success, form, dt);
            })
            .catch(err => {
                hsAfterSaveMessages(err.message || 'Request failed', false, null, null);
            })
            .finally(() => {
                hsEnableButton(submitBtn, spinner, btnText);
            });
    });
}


/* ═══════════════════════════════════════════════════════════════════════════
   §16  LEGACY / DEPRECATED  — kept for backward compatibility only
   ═══════════════════════════════════════════════════════════════════════════ */

/**
 * @deprecated  Use Swal.fire() directly or hsShowToast().
 *              $.gritter is no longer available in this codebase.
 */
function hsNotification(hsData) {
    hsShowToast(hsData.isError ? 'error' : 'success', hsData.message || '');
}

/** @deprecated */
function hsAfterDelete(hsData, rowRef) {
    hsNotification(hsData);
    if (!hsData.isError) $(rowRef).parents('tr').remove();
}

/** @deprecated */
function hsNotificationRemove() { /* gritter no longer used — no-op */ }

/**
 * @deprecated  Use Swal.fire() directly (SweetAlert2 API).
 *              The old `swal()` v1 API is not available.
 *
 * @returns {Promise<boolean>}
 */
function deleteSweetAlert(titleName, textMessage, iconName, buttonType, buttonText) {
    // FIX: migrated from swal() v1 to Swal.fire() v11 (SweetAlert2)
    return Swal.fire({
        title:              titleName,
        text:               textMessage,
        icon:               iconName,
        showCancelButton:   true,
        confirmButtonColor: buttonType === 'btn-danger' ? '#d33' : '#3085d6',
        confirmButtonText:  buttonText,
        cancelButtonText:   'Cancel'
    }).then(result => !!result.isConfirmed);
}

/**
 * @deprecated  Use Swal.fire() directly.
 */
function saveSweetAlert(saveData, stayTime) {
    // FIX: migrated from swal() v1 to Swal.fire() v11
    Swal.fire({
        icon:              saveData.isError ? 'error' : 'success',
        text:              saveData.message,
        showConfirmButton: false,
        timer:             stayTime
    });
}

/**
 * @deprecated  No-op — implement actual before-submit logic per form.
 */
function beforeFormSubmit(button) { /* implement per form if needed */ }

/**
 * @deprecated  No-op — implement actual after-submit logic per form.
 */
function afterFormSubmit(button) { /* implement per form if needed */ }
