import style from './default.js';
import {Color} from "../utils/color.js";

export default Object.entries(style).reduce((acc, [key, value]) => {
    let color = Color.fromString(value);
    if (color == null) {
        acc[key] = value;
        return acc;
    } else if (key.toLowerCase().includes("highway")
        || key.toLowerCase().includes("tunnel")
        || key.toLowerCase().includes("bridge")
        || key.toLowerCase().includes("rail")
        || key.toLowerCase().includes("ferry")) {
        acc[key] = color.darken(0.2).toString();
        return acc;
    } else {
        acc[key] = color.grayscale().lighten(0.1).toString();
        return acc;
    }
}, {});