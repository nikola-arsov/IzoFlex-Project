setInterval(() => {
    setTimeout(getComments, 15000)
}, 15000)

function getComments() {
    let id = document.getElementById("itemId").value;
    let comment_tab = document.getElementById("comment-section").children.length;

    fetch(("http://localhost:8080/comments/offers/" + id + "/refresh"))
        .then(value => value.json())
        .then(value => {
            let diff = value.length - comment_tab;

            if (diff >= 1) {
                for (let i = diff - 1; i >= 0; i--) {
                    document.getElementById("comment-section").insertAdjacentHTML("afterbegin",
                        `<li class="d-flex text-left m-1">Posted on: ${value[i].time} User: ${value[i].authorUsername} - ${value[i].content}</li>`)
                }
            }
        });
}