document.getElementById("form").addEventListener("submit", function (e) {
    let files = document.getElementById("images").files;

    for (let file of files) {
        if (file.size > 1000000) {
            e.preventDefault();
            document.getElementById("fileError").classList.add("d-flex");
            document.getElementById("images").classList.replace("text-white","text-danger")
            break;
        }
    }
})