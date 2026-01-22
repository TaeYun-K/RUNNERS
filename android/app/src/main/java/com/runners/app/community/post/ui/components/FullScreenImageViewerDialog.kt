package com.runners.app.community.post.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.ScrollGesturePropagation
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import kotlin.math.abs
import kotlin.math.roundToInt


@Composable
fun FullScreenImageViewerDialog(
    imageUrls: List<String>,
    initialIndex: Int,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (imageUrls.isEmpty()) return

    val scope = rememberCoroutineScope()

    // 세로 드래그로 닫기 오프셋
    val dismissOffsetY = remember { Animatable(0f) }

    // 각 페이지의 "줌 상태인지"만 parent에서 캐싱해서,
    // (1) pager 스와이프 허용 여부 (2) dismiss 드래그 허용 여부에 사용
    val zoomedMap = remember { mutableStateMapOf<Int, Boolean>() }

    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, imageUrls.lastIndex),
        pageCount = { imageUrls.size },
    )

    // 현재 페이지 줌 여부
    val isZoomedCurrent = zoomedMap[pagerState.currentPage] == true

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        // 아래로 당길수록 배경 투명
        val backgroundAlpha = (1f - (abs(dismissOffsetY.value) / 1000f)).coerceIn(0f, 1f)
        val backgroundColor = Color.Black.copy(alpha = backgroundAlpha)

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(backgroundColor)
                // 배경 탭으로 닫기
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onDismissRequest() })
                }
                // "줌 아닐 때만" 세로 드래그로 닫기
                .draggable(
                    orientation = Orientation.Vertical,
                    enabled = !isZoomedCurrent,
                    state = rememberDraggableState { delta ->
                        // delta: 드래그 이동량(픽셀)
                        scope.launch {
                            dismissOffsetY.snapTo(dismissOffsetY.value + delta)
                        }
                    },
                    onDragStopped = {
                        scope.launch {
                            val shouldDismiss = abs(dismissOffsetY.value) > 300f
                            if (shouldDismiss) {
                                onDismissRequest()
                            } else {
                                dismissOffsetY.animateTo(0f, animationSpec = tween(220))
                            }
                        }
                    },
                )
        ) {
            // 페이지 전환되면 dismiss 오프셋은 원복(사용자 체감상 자연스러움)
            LaunchedEffect(pagerState.currentPage) {
                dismissOffsetY.snapTo(0f)
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    // 전체 뷰를 아래로 이동 (drag-to-dismiss)
                    .offset { IntOffset(0, dismissOffsetY.value.roundToInt()) },
                // 줌 중에는 페이징 막기(제스처 충돌 방지)
                userScrollEnabled = !isZoomedCurrent,
                beyondViewportPageCount = 1,
            ) { page ->
                ZoomableCoilImage(
                    url = imageUrls[page],
                    modifier = Modifier.fillMaxSize(),
                    // page별 줌 여부를 parent로 올려서, 현재 페이지 줌이면 pager/dismiss 막기
                    onZoomedChange = { zoomed ->
                        zoomedMap[page] = zoomed
                    }
                )
            }

            // 닫기 버튼
            IconButton(
                onClick = onDismissRequest,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp)
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f)),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "닫기",
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
private fun ZoomableCoilImage(
    url: String,
    modifier: Modifier = Modifier,
    onZoomedChange: (Boolean) -> Unit,
) {
    val zoomState = rememberZoomState()
    val scope = rememberCoroutineScope()

    // scale 변화 감지해서 줌 여부 전달
    LaunchedEffect(zoomState) {
        snapshotFlow { zoomState.scale }
            .distinctUntilChanged()
            .collect { scale ->
                onZoomedChange(scale > 1.01f)
            }
    }

    // 핵심:
    // - zoomable()이 pinch/doubleTap/pan 및 경계/애니메이션을 처리
    // - NotZoomed: "줌 안 됐을 때" 스크롤 제스처를 부모(Pager)로 잘 넘김
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = url,
            contentDescription = "상세 이미지",
            contentScale = ContentScale.Fit,
            onSuccess = { state ->
                // AsyncImage 쓸 때는 로드 성공 시 intrinsicSize를 줘야 경계 계산이 정확해짐
                // (Zoomable README 권장 패턴)
                scope.launch {
                    zoomState.setContentSize(state.painter.intrinsicSize)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .zoomable(
                    zoomState = zoomState,
                    scrollGesturePropagation = ScrollGesturePropagation.NotZoomed,
                ),
        )
    }
}
