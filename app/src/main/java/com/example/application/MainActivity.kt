package com.example.application

import android.content.Context
import android.content.Intent
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit


class MainActivity : ComponentActivity() {

    private var gl: GLSurfaceView? = null
    private var renderer: MyRenderer? = null
    private var previousX = 0f
    private var previousY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getPreferences(Context.MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean("first_run", true)

        if (!isFirstRun){
            setContentView(R.layout.main)

            gl = findViewById(R.id.gl_surface_view);
            gl?.setEGLContextClientVersion(1);
            renderer = MyRenderer(this);
            gl?.setRenderer(renderer);

            gl?.setOnTouchListener(object : OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    return handleTouch(event!!)
                }
            })

            val btnLeft = findViewById<Button?>(R.id.btn_left)
            val btnRight = findViewById<Button?>(R.id.btn_right)
            val btnInfo = findViewById<Button?>(R.id.btn_info)

            btnLeft.setOnClickListener({ v ->
                renderer!!.selectPrev()
            })

            btnRight.setOnClickListener({ v ->
                renderer!!.selectNext()
            })

            btnInfo.setOnClickListener({ v ->
                val planetName = renderer!!.getSelectedPlanetName()
                for (planet in PlanetData.entries) {
                    if (planet.name == planetName) {
                        val intent: Intent =
                            Intent(this@MainActivity, InfoActivity::class.java)
                        intent.putExtra("planet_name", planet.name)
                        intent.putExtra("texture_resource", planet.textureRes)
                        intent.putExtra("planet_size", planet.size)
                        intent.putExtra("planet_info", planet.info)
                        startActivity(intent)
                        return@setOnClickListener
                    }
                }

                Toast.makeText(this@MainActivity, "Выбрана: " + planetName, Toast.LENGTH_SHORT)
                    .show()
            })
        }
        else
        {
            prefs.edit { putBoolean("first_run", false) }
            setContent {
                App(this)
            }
        }

    }

    private fun handleTouch(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                previousX = x
                previousY = y
            }

            MotionEvent.ACTION_MOVE -> if (event.pointerCount == 1) {
                val deltaX: Float = (x - previousX) / gl!!.width * 180f
                val deltaY: Float = (y - previousY) / gl!!.height * 180f

                renderer!!.moveCamera(deltaX, deltaY)

                previousX = x
                previousY = y
            }
        }

        return true
    }
}

@Composable
fun App(context: Context) {
    val newsViewModel = remember {
        NewsViewModel(context)
    }
    NewsGrid(newsViewModel = newsViewModel)
}

@Composable
fun NewsGrid(newsViewModel: NewsViewModel) {
    val displayedNews by remember { derivedStateOf { newsViewModel.displayedNews } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NewsQuarter(
                    news = displayedNews.getOrNull(0),
                    onLikeClick = { newsViewModel.likeNews(0) },
                    modifier = Modifier.weight(1f)
                )
                NewsQuarter(
                    news = displayedNews.getOrNull(1),
                    onLikeClick = { newsViewModel.likeNews(1) },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NewsQuarter(
                    news = displayedNews.getOrNull(2),
                    onLikeClick = { newsViewModel.likeNews(2) },
                    modifier = Modifier.weight(1f)
                )
                NewsQuarter(
                    news = displayedNews.getOrNull(3),
                    onLikeClick = { newsViewModel.likeNews(3) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun NewsQuarter(
    news: News?,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                if (news != null) {
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ){
                        Text(
                            text = news.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        Text(
                            text = news.content,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .weight(0.1f)
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .clickable { onLikeClick() }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👍 ",
                        fontSize = 20.sp
                    )
                    Text(
                        text = "${news?.likes ?: 0}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}