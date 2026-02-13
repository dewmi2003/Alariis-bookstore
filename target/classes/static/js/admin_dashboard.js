document.addEventListener('DOMContentLoaded', function () {
    // Order Status Donut Chart
    const statusCtx = document.getElementById('orderStatusChart').getContext('2d');
    new Chart(statusCtx, {
        type: 'doughnut',
        data: {
            labels: ['CANCELLED', 'DELIVERED', 'SHIPPED'],
            datasets: [{
                data: [5, 12, 6],
                backgroundColor: [
                    '#ef4444', // Red
                    '#10b981', // Green
                    '#06b6d4'  // Cyan
                ],
                borderWidth: 0,
                hoverOffset: 10
            }]
        },
        options: {
            cutout: '75%',
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        color: '#94a3b8',
                        padding: 20,
                        usePointStyle: true,
                        font: {
                            family: 'Inter'
                        }
                    }
                }
            },
            maintainAspectRatio: false
        }
    });

    // Inventory Health Gauge Chart
    const healthCtx = document.getElementById('inventoryHealthChart').getContext('2d');
    new Chart(healthCtx, {
        type: 'doughnut',
        data: {
            datasets: [{
                data: [85, 15],
                backgroundColor: [
                    '#10b981', // Green
                    'rgba(255, 255, 255, 0.05)'
                ],
                circumference: 240,
                rotation: 240,
                borderWidth: 0,
                borderRadius: 20
            }]
        },
        options: {
            cutout: '85%',
            plugins: {
                legend: { display: false },
                tooltip: { enabled: false }
            },
            maintainAspectRatio: false
        }
    });

    // Hover sound/micro-animation simulation
    const cards = document.querySelectorAll('.glass-card');
    cards.forEach(card => {
        card.addEventListener('mouseenter', () => {
            card.style.borderColor = 'rgba(124, 58, 237, 0.5)';
        });
        card.addEventListener('mouseleave', () => {
            card.style.borderColor = 'rgba(255, 255, 255, 0.08)';
        });
    });
});
