document.getElementById("form").addEventListener("submit", (x) => {
    let submitButton = document.getElementById("submit");

    submitButton.disabled = true;

    setTimeout(() => {
        submitButton.disabled = false;
    }, 3000);

});