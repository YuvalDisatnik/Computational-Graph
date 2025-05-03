document.getElementById('chooseFileBtn').addEventListener('click', function() {
        document.getElementById('configFile').click();
    });

    document.getElementById('configFile').addEventListener('change', function() {
        const fileName = this.files[0] ? this.files[0].name : 'No file chosen';
        document.getElementById('fileName').textContent = fileName;
    });