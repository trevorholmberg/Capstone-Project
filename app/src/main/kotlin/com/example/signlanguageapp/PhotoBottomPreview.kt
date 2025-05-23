package com.example.signlanguageapp

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp

/**
 * Displays a grid of photos in a staggered layout.
 * If no photos are provided, a message is shown instead.
 * This function checks if the provided `bitmaps` list is empty.
 * If there are no photos, it displays a message indicating that no photos are available.
 * If there are photos, it shows them in a staggered grid layout,
 *
 * @param bitmaps A list of `Bitmap` objects to display in the grid.
 * @param modifier A Modifier applied to the entire composable.
 */
@Composable
fun PhotoBottomPreview(bitmaps: List<Bitmap>, modifier: Modifier = Modifier) {
    val smallPadding = 16
    val cells = 6
    val smallPercent = 10

    if (bitmaps.isEmpty()) {
        Box(
            modifier = modifier
                .padding(smallPadding.dp),
            contentAlignment = Alignment.Center

        )
        {
            Text("There are no photos")
        }
    } else {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(cells),
            horizontalArrangement = Arrangement.spacedBy(smallPadding.dp),
            verticalItemSpacing = smallPadding.dp,
            contentPadding = PaddingValues(smallPadding.dp),
            modifier = modifier
        )

        {
            items(bitmaps) { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = modifier
                        .clip(RoundedCornerShape(smallPercent))

                )



                }

            }

        }

    }



