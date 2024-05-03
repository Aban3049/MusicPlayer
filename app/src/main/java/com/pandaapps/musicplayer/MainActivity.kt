package com.pandaapps.musicplayer


import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.pandaapps.musicplayer.ui.theme.MusicPlayerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.absoluteValue


@OptIn(ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {

    private lateinit var player: ExoPlayer


    @OptIn(ExperimentalAnimationApi::class)
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = ExoPlayer.Builder(this).build()
        enableEdgeToEdge()
        setContent {
            MusicPlayerTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {

                    val uiController = rememberSystemUiController()

                    val color = listOf(
                        Color(0xFFFF5A5A),
                        Color(0xFFFFBE3D),
                        Color(0xFFD3FF5A),
                        Color(0xFF5AFFB8),
                        Color(0xFF5AFAFF),
                        Color(0xFF5A9CFF),
                        Color(0xFF6A5AFF),
                        Color(0xFFAD5AFF),
                        Color(0xFFFF5A94),

                        )

                    val darkColors = listOf(
                        Color(0xFFBD3030),
                        Color(0xFFAF8024),
                        Color(0xFF83A525),
                        Color(0xFF2B8D63),
                        Color(0xFF288D91),
                        Color(0xFF294E85),
                        Color(0xFF2E248D),
                        Color(0xFF5C1F7E),
                        Color(0xFF812344)

                    )

                    val colorIndex = remember {
                        mutableStateOf(0)
                    }
                    LaunchedEffect(Unit) {
                        while (isActive) {
                            delay(2100)
                            colorIndex.value = (colorIndex.value + 1) % color.size
                        }
                    }
                    LaunchedEffect(colorIndex.value) {
                        delay(2100)

                        if (colorIndex.value < darkColors.lastIndex) {
                            colorIndex.value += 1
                        } else {
                            colorIndex.value = 0
                        }

                    }


                    val animatedColor = animateColorAsState(
                        targetValue = color[colorIndex.value], animationSpec = tween(2000),
                        label = "animateColorAsState"
                    )

                    val animateDarkColor = animateColorAsState(
                        targetValue = darkColors[colorIndex.value], animationSpec = tween(2000),
                        label = "animateDarkColorAsState"
                    )

                    uiController.setStatusBarColor(animatedColor.value, darkIcons = false)
                    uiController.setNavigationBarColor(animatedColor.value)


                    val music = remember {
                        listOf(
                            Music(
                                name = "Blood",
                                cover = R.drawable.song_cover_one,
                                music = R.raw.blood
                            ),
                            Music(
                                name = "Destiny",
                                cover = R.drawable.song_cover_two,
                                music = R.raw.destiny
                            ),
                            Music(
                                name = "F** Haters",
                                cover = R.drawable.song_cover_three,
                                music = R.raw.fuckhaters
                            ),
                        )
                    }
                    PrepareMusicPlayer(music, packageName, player)


                    val pageState = rememberPagerState(pageCount = { music.size })
//                    synchronizePlayerWithPagerState(pageState)

                    val playingIndex = remember {
                        mutableStateOf(0)
                    }

                    LaunchedEffect(pageState.currentPage) {
                        playingIndex.value = pageState.currentPage
                        player.seekTo(pageState.currentPage, 0)
                    }



                    player.prepare()

                    val playing = remember {
                        mutableStateOf(false)
                    }

                    val currentPosition = remember {
                        mutableLongStateOf(0)
                    }

                    val totalDuration = remember {

                        mutableLongStateOf(0)
                    }

                    val progressSize = remember {

                        mutableStateOf(IntSize(0, 0))
                    }

                    LaunchedEffect(player.isPlaying) {
                        playing.value = player.isPlaying
                    }

                    LaunchedEffect(player.currentPosition) {
                        currentPosition.longValue = player.currentPosition
                    }

                    LaunchedEffect(player.duration) {
                        if (player.duration > 0) {
                            totalDuration.longValue = player.duration
                        }

                    }

                    LaunchedEffect(player.currentMediaItemIndex) {
                        playingIndex.value = player.currentMediaItemIndex
                        pageState.animateScrollToPage(
                            playingIndex.value, animationSpec = tween(500)
                        )
                    }

                    var percentReached =
                        currentPosition.longValue.toFloat() / (if (totalDuration.longValue > 0) totalDuration.longValue else 0).toFloat()
                    if (percentReached.isNaN()) {
                        percentReached = 0f
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        animatedColor.value, animateDarkColor.value
                                    )
                                )
                            ), contentAlignment = Alignment.Center
                    ) {
                        val configuration = LocalConfiguration.current



                        Column(horizontalAlignment = Alignment.CenterHorizontally) {


                            val safeIndex =
                                if (playingIndex.value >= 0 && playingIndex.value < music.size) {
                                    playingIndex.value
                                } else {
                                    music.size - 1
                                }

                            val textColor by animateColorAsState(
                                targetValue = if (animatedColor.value.luminance() > .5f) Color(
                                    0xff414141
                                ) else Color.White,
                                animationSpec = tween(2000),
                                label = "animateColorAsState"
                            )

                            AnimatedContent(targetState = safeIndex, transitionSpec = {
                                (scaleIn() + fadeIn()) with (scaleOut() + fadeOut())
                            }, label = "animationContent") {

                                Text(
                                    text = music[it].name,
                                    fontSize = 52.sp,
                                    color = textColor
                                )
                            }




                            Spacer(modifier = Modifier.height(32.dp))

                            HorizontalPager(
                                modifier = Modifier.fillMaxWidth(),
                                state = pageState,
                                pageSize = PageSize.Fixed((configuration.screenWidthDp / (1.7)).dp),
                                contentPadding = PaddingValues(horizontal = 85.dp)
                            )

                            { page ->
                                Card(modifier = Modifier
                                    .size((configuration.screenWidthDp / (1.7)).dp)
                                    .graphicsLayer {
                                        val pageOffset =
                                            ((pageState.currentPage - page) * pageState.currentPageOffsetFraction).absoluteValue

                                        val alphaLerp = lerp(
                                            start = 0.5f,
                                            stop = 1f,
                                            amount = 1f - pageOffset.coerceIn(0f, .5f)

                                        )


                                        val scaleLerp = lerp(
                                            start = 0.4f,
                                            stop = 1f,
                                            amount = 1f - pageOffset.coerceIn(0f, 1f)

                                        )



                                        alpha = alphaLerp
                                        scaleX = scaleLerp
                                        scaleY = scaleLerp


                                    }
                                    .border(2.dp, Color.White, CircleShape)
                                    .padding(6.dp),


                                    shape = CircleShape) {
                                    Image(
                                        painterResource(id = music[page].cover),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(54.dp))
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = convertLongToText(currentPosition.longValue),
                                    modifier = Modifier.width(55.dp),
                                    color = textColor,
                                    textAlign = TextAlign.Center
                                )

                                //progress BOx
                                Box(modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .height(8.dp)
                                    .padding(horizontal = 8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .onGloballyPositioned {
                                        progressSize.value = it.size
                                    }
                                    .pointerInput(Unit) {
                                        detectTapGestures {
                                            val xPos = it.x
                                            val whereIClicked =
                                                (xPos.toLong() * totalDuration.longValue) / progressSize.value.width.toLong()
                                            player.seekTo((whereIClicked))
                                        }
                                    }, contentAlignment = Alignment.CenterStart
                                ) {

                                    // Status Box
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction = if (playing.value) percentReached else 0f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xff414141))
                                    )

                                }

                                Text(
                                    text = convertLongToText(totalDuration.longValue),
                                    modifier = Modifier.width(55.dp),
                                    color = textColor,
                                    textAlign = TextAlign.Center
                                )

                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Control(icon = R.drawable.ic_rewind, size = 60.dp, onClick = {
                                    player.seekToPreviousMediaItem()
                                })

                                Control(icon = if (playing.value) R.drawable.ic_pause else R.drawable.ic_play,
                                    size = 80.dp,
                                    onClick = {
                                        if (playing.value) {
                                            player.pause()
                                        } else {
                                            player.play()
                                        }
                                    })

                                Control(
                                    icon = R.drawable.ic_fast_forward,
                                    size = 60.dp,
                                    onClick = {
                                        player.seekToNextMediaItem()
                                    })


                            }


                        }
                    }

                }

            }
        }
    }


}

fun <T> List<T>.getSafely(index: Int?): T? =
    when (index) {
        null -> this.getOrNull(this.lastIndex) // When index is null, attempt to return the first element
        in indices -> this[index] // Valid index within bounds
        else -> null // Index out of bounds or invalid
    }

@Composable
fun PrepareMusicPlayer(musicList: List<Music>, packageName: String, player: ExoPlayer) {
    musicList.forEach {
        val path = "android.resource://$packageName/${it.music}"
        val mediaItem = MediaItem.fromUri(Uri.parse(path))
        player.addMediaItem(mediaItem)
    }

    val safeIndex: Int? = null
    val safeMusicItem = musicList.getSafely(safeIndex)
    safeMusicItem?.let {
        // You can safely use safeMusicItem here, knowing it either represents the first item or a valid item by index.
        val path = "android.resource://$packageName/${it.music}"
        val mediaItem = MediaItem.fromUri(Uri.parse(path))
        player.addMediaItem(mediaItem)
    }
}


@Composable
fun Control(icon: Int, size: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White)
            .clickable {
                onClick()
            }, contentAlignment = Alignment.Center
    ) {

        Icon(
            painterResource(id = icon),
            tint = Color(0xff414141),
            contentDescription = null,
            modifier = Modifier.size(size / 2)
        )

    }
}

fun convertLongToText(long: Long): String {
    val sec = long / 1000
    val minute = sec / 60
    val seconds = sec % 60

    val minutesString = if (minute < 10) {
        "0${minute}"
    } else {
        minute.toString()
    }

    val secondString = if (seconds < 10) {
        "0${seconds}"
    } else {
        seconds.toString()
    }

    return "$minutesString:$secondString"

}

data class Music(
    val name: String,
    val music: Int,
    val cover: Int,
)
