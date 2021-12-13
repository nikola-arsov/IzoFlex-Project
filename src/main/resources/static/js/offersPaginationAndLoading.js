document.getElementById("main").addEventListener("click", (target) => {
    if (target.target.tagName === "A" && target.target.href.startsWith("http://localhost:8080/offers/categories/")) {
        target.preventDefault();
        document.getElementById("tab").innerHTML = "";

        for (let child of document.getElementById("menu").children) {
            let dete = Array.from(child.children)[0];
            dete.classList.remove("active-category");

            if (target.target.href.includes(dete.id)) {
                dete.classList.add("active-category")
            }
        }

        let parts = target.target.href.split("?");
        window.history.replaceState(null, "", parts[0] + "/view" + ((parts[1]) ? ("?" + parts[1]) : ""))

        fetchData(target.target.href);
    }
});

function fetchData(url) {
    fetch(url)
        .then(value => value.json())
        .then(value => {
            let currIndex = value.number;
            let maxIndex = value.totalPages - 1;
            modifyPaginationUI(currIndex, maxIndex);

            for (let offer of value.content) {
                document.getElementById("tab").insertAdjacentHTML("beforeend",
                    `<div class="offer card col-sm-6 col-md-3  col-lg-2 m-1 p-0" style="max-width: 260px">
                              <div class="card-img-top-wrapper">
                              <img class="card-img-top h-100" style="object-fit: cover;" src="${offer.imageLocation}" alt="Car image">
                              </div>
                              <div class="card-body p-2">
                              <h5 class="card-title mb-0 text-center ">${offer.itemName}</h5>
                              </div>
                              <ul class="offer-details list-group list-group-flush d-block">
                              <li class="list-group-item position-static">
                              <div class="card-text">Категория: ${offer.itemCategory.charAt(0) + offer.itemCategory.slice(1).toLocaleLowerCase()}</div>
                              <div class="card-text"><span>Продавач: ${offer.sellerUsername}</span></div>
                              <div class="card-tex t"><span>Цена: ${Number(offer.price).toFixed(2)} лв.</span></div>
                              </li>
                              </ul>
                              <div class="card-body p-2">
                              <a class="card-link" href="/offers/details/${offer.id}">Детайли на офертата</a>
                              </div>
                              </div>`)
            }
        });
}

function modifyPaginationUI(currIndex, maxIndex) {
    let prevLi = document.getElementById("prev-li");
    document.getElementById("current").textContent = currIndex;
    let nextLi = document.getElementById("next-li");

    togglePaginationCSS(prevLi, currIndex, (currIndex - 1 < 0), "-");
    togglePaginationCSS(nextLi, currIndex, (currIndex >= maxIndex), "+");
}

function togglePaginationCSS(element, currIndex, flag, operation) {
    if (flag) {
        element.classList.add("disabled");
        element.firstElementChild.classList.replace("bg-secondary", "bg-dark");
    } else {
        element.classList.remove("disabled");
        element.firstElementChild.classList.replace("bg-dark", "bg-secondary");
        element.firstElementChild.href = document.querySelector("#menu .active-category").href + "?page=" + (("+" === operation) ? currIndex + 1 : currIndex - 1);
    }
}