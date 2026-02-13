/**
 * Admin Dashboard Interactivity
 * Handles: Charts (Chart.js), Counters (CountUp.js), and Table Sorting.
 */

document.addEventListener('DOMContentLoaded', function () {
    console.log('Admin Dashboard JS Initialized');
    if (typeof dashboardData === 'undefined') return;

    // 1. Initialize Animated Counters
    initializeCounters();

    // 2. Initialize Charts
    initializeCharts();

    // 3. Initialize Table Sorting
    initializeTableSorting();
});

function initializeCounters() {
    const options = {
        duration: 2,
        useEasing: true,
        useGrouping: true,
        separator: ',',
        decimal: '.',
    };

    const countConfigs = [
        { id: 'todaySales', value: dashboardData.counts.todaySales, decimals: 2 },
        { id: 'monthSales', value: dashboardData.counts.monthSales, decimals: 2 },
        { id: 'totalOrders', value: dashboardData.counts.totalOrders, decimals: 0 },
        { id: 'pendingOrders', value: dashboardData.counts.pendingOrders, decimals: 0 },
        { id: 'totalBooks', value: dashboardData.counts.totalBooks, decimals: 0 },
        { id: 'totalUsers', value: dashboardData.counts.totalUsers, decimals: 0 },
        { id: 'lowStockItems', value: dashboardData.counts.lowStockItems, decimals: 0 }
    ];

    countConfigs.forEach(config => {
        const el = document.getElementById(config.id);
        if (el && typeof countUp !== 'undefined') {
            const demo = new countUp.CountUp(config.id, config.value, { ...options, decimalPlaces: config.decimals });
            if (!demo.error) {
                demo.start();
            } else {
                console.error(demo.error);
            }
        }
    });
}

function initializeCharts() {
    if (typeof Chart === 'undefined') return;


    // --- Order Status Chart (Doughnut) ---
    const statusCtx = document.getElementById('orderStatusChart');
    if (statusCtx) {
        const labels = Object.keys(dashboardData.orderStatus);
        const values = Object.values(dashboardData.orderStatus);
        const colors = {
            'PENDING': '#f59e0b',
            'SHIPPED': '#3b82f6',
            'DELIVERED': '#10b981',
            'CANCELLED': '#ef4444'
        };

        new Chart(statusCtx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: values,
                    backgroundColor: labels.map(l => colors[l] || '#6b7280'),
                    hoverOffset: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'bottom' }
                }
            }
        });
    }



    // --- Low Stock Distribution Chart (Pie) ---
    const lowStockCtx = document.getElementById('lowStockChart');
    if (lowStockCtx) {
        const healthyItems = dashboardData.counts.totalBooks - dashboardData.counts.lowStockItems;
        new Chart(lowStockCtx, {
            type: 'pie',
            data: {
                labels: ['Healthy Stock', 'Low Stock (<= 5)'],
                datasets: [{
                    data: [healthyItems, dashboardData.counts.lowStockItems],
                    backgroundColor: ['#10b981', '#ef4444'],
                    hoverOffset: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'bottom' }
                }
            }
        });
    }

    // --- 7. Auto-Refresh Stats (every 60s) ---
    setInterval(() => {
        console.log('Refreshing dashboard data...');
        location.reload();
    }, 60000);
}

function initializeTableSorting() {
    const getCellValue = (tr, idx) => tr.children[idx].innerText || tr.children[idx].textContent;
    const comparer = (idx, asc) => (a, b) => ((v1, v2) =>
        v1 !== '' && v2 !== '' && !isNaN(v1) && !isNaN(v2) ? v1 - v2 : v1.toString().localeCompare(v2)
    )(getCellValue(asc ? a : b, idx), getCellValue(asc ? b : a, idx));

    document.querySelectorAll('th').forEach(th => {
        // Add sortable class to headers that should be sortable (Title, Price, Stock, Category)
        const sortableFields = ['Title', 'Price', 'Stock', 'Category', 'Total', 'Status', 'Date', 'Amount'];
        if (sortableFields.some(field => th.innerText.includes(field))) {
            th.classList.add('sortable-header');
            th.addEventListener('click', () => {
                const table = th.closest('table');
                const tbody = table.querySelector('tbody');
                Array.from(tbody.querySelectorAll('tr'))
                    .sort(comparer(Array.from(th.parentNode.children).indexOf(th), this.asc = !this.asc))
                    .forEach(tr => tbody.appendChild(tr));

                // Toggle classes for visual indicator
                th.parentNode.querySelectorAll('th').forEach(h => h.classList.remove('sort-asc', 'sort-desc'));
                th.classList.add(this.asc ? 'sort-asc' : 'sort-desc');
            });
        }
    });
}
