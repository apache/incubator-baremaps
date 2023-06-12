import style from './default.js';
import * as color from "../utils/color.js";

const min = 0;
const max = 255;

function positronScheme(string) {
    const table = cutRgbString(string)
    // console.log(color.readColor(string));
    let newColor = positronColorCalculator(table);
    return giveNewColorString(table, newColor, newColor, newColor);
};

function positronColorCalculator(table) {
    return Math.round((parseInt(table[0]) + parseInt(table[1]) + parseInt(table[2])) / 3);
};

function moreRedScheme(string) {
    const table = cutRgbString(string);
    let redElement = Math.min(Math.max(table[0] + 50, min), max);
    return giveNewColorString(table, redElement, table[1], table[2]);
}

function giveNewColorString(table, newRed, newGreen, newBlue) {
    if (table[3] == 1) {
        return `rgb(${newRed},${newGreen},${newBlue})`;
    } else {
        let opacity = table[3];
        return `rgba(${newRed},${newGreen},${newBlue},${opacity})`;
    }
};

function cutRgbString(string) {
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
        console.log("opa " + opacity);
    } else {
        blueElement = greenSubstring.substring(greenSubstring.indexOf(middleSeparationCharacter) + 1, greenSubstring.indexOf(lastSeparationCharacter));
        opacity = "1"
    }

    return [redElement, greenElement, blueElement, opacity];
};

function hasOpacity(string) {
    return (string.substring(0, 5) === "rgba(");
};

export default Object.entries(style).reduce((acc, [key, value]) => {
    acc[key] = positronScheme(value);
    return acc;
}, {});