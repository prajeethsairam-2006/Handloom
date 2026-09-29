// Handloom Marketplace - Clean Application Scripts

document.addEventListener('DOMContentLoaded', function () {
    // Auto-dismiss alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });

    // Image preview for file uploads
    const fileInputs = document.querySelectorAll('input[type="file"][data-preview-target]');
    fileInputs.forEach(function (input) {
        input.addEventListener('change', function (event) {
            const targetId = input.getAttribute('data-preview-target');
            const previewImg = document.getElementById(targetId);
            if (previewImg && event.target.files && event.target.files[0]) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    previewImg.src = e.target.result;
                    previewImg.classList.remove('d-none');
                };
                reader.readAsDataURL(event.target.files[0]);
            }
        });
    });

    // Quantity selector buttons
    document.querySelectorAll('.qty-btn-decrease').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const input = this.closest('.qty-control').querySelector('input');
            let val = parseInt(input.value) || 1;
            if (val > 1) {
                input.value = val - 1;
                // If in a form that submits on change
                if (this.dataset.autoSubmit === 'true') {
                    this.closest('form').submit();
                }
            }
        });
    });

    document.querySelectorAll('.qty-btn-increase').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const input = this.closest('.qty-control').querySelector('input');
            let max = parseInt(input.getAttribute('max')) || 999;
            let val = parseInt(input.value) || 1;
            if (val < max) {
                input.value = val + 1;
                if (this.dataset.autoSubmit === 'true') {
                    this.closest('form').submit();
                }
            }
        });
    });
});
