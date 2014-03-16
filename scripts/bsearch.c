#include <stdlib.h>

/*
 * Passing functions as arguments is not alien to imperative programming.
 * Below a pointer to function allows for generic binary search
 */
void * binary_search (
    void *key, // The search key
    void *base, // The sorted array's initial address
    int num, // The sorted array's number of elements
    int width, // The width of each element in the array
    int (*compare)(void *, void *) // The comparison function
)
{
    void *lo = base;
    void *hi = base + (num - 1) * width;
    void *mid;
    int half;
    int result;

    while (lo <= hi)
        if (half = num / 2) {
            mid = lo + (num & 1 ? half : (half - 1)) * width;
            if (!(result = (*compare)(key,mid)))
                return(mid);
            else if (result < 0) {
                hi = mid - width;
                num = num & 1 ? half : half-1;
            }
            else    {
                lo = mid + width;
                num = half;
            }
        }
        else if (num)
            return((*compare)(key,lo) ? NULL : lo);
        else
            break;

    return(NULL);
}

