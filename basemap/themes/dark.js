import style from './light.js';
import {Color} from "../utils/color.js";

export default Object.entries(style).reduce((acc, [key, value]) => {
    let color = Color.fromString(value);
    if (color == null) {
        acc[key] = value;
        return acc;
    } else {
        acc[key] = color.grayscale().invert().toString();
        return acc;
    }
}, {});
