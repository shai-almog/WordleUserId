const gameBoard = document.getElementById("game-board");

const maxAttempts = 6;
let currentRow = 0;
let currentColumn = 0;
let currentGuess = '';


function getCookie(name) {
    const cookieName = name + "=";
    const cookies = document.cookie.split(';');
    for (let i = 0; i < cookies.length; i++) {
        let cookie = cookies[i].trim();
        if (cookie.indexOf(cookieName) === 0) {
            return cookie.substring(cookieName.length, cookie.length);
        }
    }
    return "";
}

function createUserIDCookie() {
    const userIdCookieName = "userId";
    let userID = getCookie(userIdCookieName);

    if (!userID) {
        userID = uuidv4();
        const date = new Date();

        // The date the cookie should expire which is 10 years in the future
        date.setTime(date.getTime() + (3650 * 24 * 60 * 60 * 1000));
        const expires = `expires=${date.toUTCString()}`;
        document.cookie = `${userIdCookieName}=${userID};${expires};path=/`;
    }

    return userID;
}

const userID = createUserIDCookie();




function generateBoard() {
    for (let row = 0; row < maxAttempts; row++) {
        const rowElement = document.createElement("div");
        rowElement.classList.add("row");

        for (let column = 0; column < 5; column++) {
            const letterBox = document.createElement("div");
            letterBox.classList.add("letter-box");
            letterBox.id = `letter-${row}-${column}`;
            rowElement.appendChild(letterBox);
        }

        gameBoard.appendChild(rowElement);
    }
}

// Fetch the guess result from the web service
async function checkGuess(guess) {
    const response = await fetch('/guess?word=' + guess, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        }
    });
    const result = await response.json();
    return result;
}

function updateBoard(result) {
    for (let i = 0; i < result.length; i++) {
        const letterBox = document.getElementById(`letter-${currentRow}-${i}`);

        letterBox.textContent = currentGuess[i];

        switch (result[i]) {
            case "GREEN":
                letterBox.style.backgroundColor = "green";
                letterBox.style.color = "white";
                break;
            case "YELLOW":
                letterBox.style.backgroundColor = "yellow";
                letterBox.style.color = "black";
                break;
            case "BLACK":
                letterBox.style.backgroundColor = "black";
                letterBox.style.color = "white";
                break;
        }
    }
}

function isWin(result) {
    for (let i = 0; i < result.length; i++) {
        if(result[i] !== "GREEN") {
            return false;
        }
    }
    return true;
}

document.addEventListener("keydown", async (event) => {
    const letter = event.key.toLowerCase();

    if (letter.match(/^[a-z]$/) && currentColumn < 5) {
        const letterBox = document.getElementById(`letter-${currentRow}-${currentColumn}`);
        letterBox.textContent = letter;
        currentGuess += letter;
        currentColumn++;
    } else if (event.code === "Backspace" && currentColumn > 0) {
        currentColumn--;
        currentGuess = currentGuess.slice(0, -1);
        const letterBox = document.getElementById(`letter-${currentRow}-${currentColumn}`);
        letterBox.textContent = "";
    }

    if (currentColumn >= 5) {
        const result = await checkGuess(currentGuess);
        if(result.results !== null) {
            updateBoard(result.results);
        } else {
            alert(result.errorMessage);
        }

        if (isWin(result.results)) {
            setTimeout(() => {
                alert("Congratulations! You've won!");
                location.reload();
            }, 500);
        } else {
            currentRow++;
            if (currentRow >= maxAttempts) {
                setTimeout(() => {
                    alert("You've run out of attempts!");
                    location.reload();
                }, 500);
            } else {
                currentColumn = 0;
                currentGuess = '';
            }
        }
    }
});

generateBoard();

