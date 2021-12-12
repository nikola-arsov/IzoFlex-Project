document.getElementById("form").addEventListener("submit", function (event) {
    let price = document.getElementById("price");
    let error = document.getElementById("price-error");
    let label = document.getElementById("label");
    let regExp =new RegExp("[\\d\.]+")

    if (price.value < 0.10|| price.getAttribute("type")!=="number"||!regExp.text(price.value)) {
        event.preventDefault();
        error.style.display = "block";
        label.style.display = "none";
        price.classList.add("border-danger");

        setTimeout(() => {
            error.style.display = "none";
            label.style.display = "block";
            price.setAttribute("type","number");
        }, 3000);
    }
});