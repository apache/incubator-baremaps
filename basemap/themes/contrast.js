import style from './default.js';
import {Color} from "../utils/color.js";

export default Object.entries(style).reduce((acc, [key, value]) => {
    let color = Color.fromString(value);
    if (color == null) {
        acc[key] = value;
        return acc;
    } else {
        acc[key] = color.contrast(0.1).toString();
        return acc;
    }
}, {});
