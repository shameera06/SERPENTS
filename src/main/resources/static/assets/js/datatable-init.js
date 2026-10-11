/**
 * Reusable DataTables initialization helper for the admin panel.
 *
 * This is a thin wrapper around jQuery DataTables (already vendored at
 * assets/vendor/libs/datatables-bs5/) that applies this project's shared
 * defaults — comfortable page-length options, the standard "Show entries"
 * (top-left) / "Search" (top-right) / table / "Showing x to y of z entries"
 * (bottom-left) / pagination (bottom-right) layout seen in every module's
 * listing — while letting a module override or extend anything it needs
 * (columns, ajax, order, serverSide, ...) for its own data.
 *
 * NOT wired to any module/table by default — a page opts in explicitly:
 *
 *   @push('page-scripts')
 *   <script src="{{ asset('assets/vendor/libs/datatables-bs5/datatables.js') }}"></script>
 *   <script src="{{ asset('assets/vendor/libs/datatables-bs5/datatables.bootstrap5.js') }}"></script>
 *   <script src="{{ asset('assets/js/datatable-init.js') }}"></script>
 *   <script>
 *       initDataTable('#species-table', {
 *           serverSide: true,
 *           ajax: '{{ route('admin.species.index') }}',
 *           columns: [
 *               { data: 'name', name: 'name' },
 *               { data: 'status', name: 'status' },
 *               { data: 'actions', name: 'actions', orderable: false, searchable: false },
 *           ],
 *       });
 *   </script>
 *   @endpush
 *
 * A plain client-side table (no `ajax`/`serverSide`) works the same way —
 * just omit those two options and DataTables reads the existing <tbody> rows.
 *
 * Yajra's server-side response format (`draw`/`recordsTotal`/
 * `recordsFiltered`/`data`) is exactly what `serverSide: true` expects, so a
 * module's own Yajra DataTable class (see app/DataTables/) is a drop-in
 * `ajax` source — no changes needed here when a module adds one.
 */
(function (window, $) {
    'use strict';

    if (typeof $ === 'undefined' || !$.fn || !$.fn.DataTable) {
        // DataTables (and its jQuery dependency) not loaded on this page —
        // nothing to do. Pages that don't use a DataTable never load this
        // file, so this only guards against an incorrect include order.
        return;
    }

    var DEFAULTS = {
        // Matches the reference design: length menu + search on top,
        // the table itself, then info + pagination on the bottom.
        dom:
            "<'row mb-3'<'col-sm-6'l><'col-sm-6'f>>" +
            "<'row'<'col-sm-12't>>" +
            "<'row mt-3'<'col-sm-6'i><'col-sm-6'p>>",
        lengthMenu: [10, 25, 50, 100],
        pageLength: 10,
        responsive: true,
        language: {
            search: '_INPUT_',
            searchPlaceholder: 'Search...',
            lengthMenu: 'Show _MENU_ entries',
            info: 'Showing _START_ to _END_ of _TOTAL_ entries',
            infoEmpty: 'Showing 0 to 0 of 0 entries',
            infoFiltered: '(filtered from _MAX_ total entries)',
            paginate: {
                previous: '«',
                next: '»',
            },
        },
    };

    /**
     * Initialize a DataTable on the given selector, merging this project's
     * shared defaults with whatever options the calling page provides.
     *
     * @param {string} selector - a table selector, e.g. '#species-table'.
     * @param {object} [options] - DataTables options; see https://datatables.net/reference/option/
     * @returns {object} the DataTables API instance.
     */
    window.initDataTable = function initDataTable(selector, options) {
        var settings = $.extend(true, {}, DEFAULTS, options || {});

        return $(selector).DataTable(settings);
    };
})(window, window.jQuery);
