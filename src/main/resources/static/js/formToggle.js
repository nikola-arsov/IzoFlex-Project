const button = document.getElementById("sell");

button.addEventListener("click", (event) => {
    event.preventDefault();

    let form = document.getElementById("form");
    let description = document.getElementById("description");

    if (form.classList.contains("d-none")) {
        editClasses(form, description, "d-none", "d-inline-flex", "none");
    } else {
        editClasses(form, description, "d-inline-flex", "d-none", "inline-block");
    }
});

function editClasses(form, description, one, two, three) {
    form.classList.remove(one);
    form.classList.add(two)
    description.style.display = three;
    button.textContent = (button.textContent === "Сложете цена на офертата") ? "Скрийте менюто за цена" : "Сложете цена на офертата";
}

