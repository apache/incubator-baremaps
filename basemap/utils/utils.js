export function withFillSortKey(directive, index, array) {
    return {
        ...directive,
        'fill-sort-key': array.length - index - 1,
    }
}

export function withLineSortKey(directive, index, array) {
    return {
        ...directive,
        'line-sort-key': array.length - index - 1,
    }
}