
const min = 0;
const max = 255;


export function giveNewColorString(table, newRed, newGreen, newBlue) {
    if (table[3] == 1) {
        return `rgb(${newRed},${newGreen},${newBlue})`;
    } else {
        let opacity = table[3];
        return `rgba(${newRed},${newGreen},${newBlue},${opacity})`;
    }
};

export function cutRgbString(string) {
    const firstSeparationCharacter = "(";
    const middleSeparationCharacter = ",";
    const lastSeparationCharacter = ")";

    let greenSubstring = string.substring(string.indexOf(middleSeparationCharacter) + 1, string.length);

    let redElement = string.substring(string.indexOf(firstSeparationCharacter) + 1, string.indexOf(middleSeparationCharacter));
    let greenElement = greenSubstring.substring(0, greenSubstring.indexOf(middleSeparationCharacter));
    let blueElement;
    let opacity;

    if (hasOpacity(string)) {
        let blueSubstring = greenSubstring.substring(greenSubstring.indexOf(middleSeparationCharacter) + 1, greenSubstring.length);
        blueElement = blueSubstring.substring(0, blueSubstring.indexOf(middleSeparationCharacter));
        opacity = blueSubstring.substring(blueSubstring.indexOf(middleSeparationCharacter) + 1, blueSubstring.length - 1)
    } else {
        blueElement = greenSubstring.substring(greenSubstring.indexOf(middleSeparationCharacter) + 1, greenSubstring.indexOf(lastSeparationCharacter));
        opacity = "1"
    }

    return [redElement, greenElement, blueElement, opacity];
};

function hasOpacity(string) {
    return (string.substring(0, 5) === "rgba(");
};