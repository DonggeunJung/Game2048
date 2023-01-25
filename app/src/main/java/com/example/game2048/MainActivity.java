package com.example.game2048;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements JGameLib.GameEvent {
    int blockCount = 4;
    JGameLib gameLib = null;
    JGameLib.Card[][] numCards = new JGameLib.Card[blockCount][blockCount];
    final float edgeThick = 0.1f;
    final int edgeColor = Color.rgb(192,175,157);
    final int colorEmpty = Color.rgb(207,194,178);
    final int color2back = Color.rgb(227,217,207);
    final int color2text = Color.rgb(151,141,131);
    float touchX = -1, touchY = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameLib = findViewById(R.id.gameLib);
        initGame();
    }

    @Override
    protected void onDestroy() {
        if(gameLib != null)
            gameLib.clearMemory();
        super.onDestroy();
    }

    void initGame() {
        gameLib.listener(this);
        gameLib.setScreenGrid(blockCount+edgeThick*2, blockCount+edgeThick*2);
        gameLib.addCardColor(edgeColor);
        for(int y=0; y < blockCount; y++) {
            for(int x=0; x < blockCount; x++) {
                numCards[y][x] = gameLib.addCardColor(colorEmpty,x+edgeThick,y+edgeThick,1,1);
                numCards[y][x].edge(edgeColor, edgeThick);
                numCards[y][x].text("", color2text, 0.5);
            }
        }
        restart();
    }

    void restart() {
        for(int y=0; y < blockCount; y++) {
            for(int x=0; x < blockCount; x++) {
                numCards[y][x].backColor(colorEmpty);
                numCards[y][x].text("", color2text, 0.5);
            }
        }
        add2or4();
        add2or4();
    }

    void add2or4() {
        int cells = blockCount * blockCount;
        int n = 2;
        if(gameLib.random(5) == 0)
            n = 4;
        while(true) {
            int cellNum = gameLib.random(cells);
            int y = cellNum / blockCount;
            int x = cellNum % blockCount;
            if(numCards[y][x].isTextEmpty()) {
                numCards[y][x].text(n);
                numCards[y][x].backColor(color2back);
                return;
            }
        }
    }

    boolean slidePushHrz(JGameLib.DirType dir) {
        boolean res = false;
        int gapX=-1, beginX=0;
        if(dir == JGameLib.DirType.LEFT) {
            gapX=1;
        } else if(dir == JGameLib.DirType.RIGHT) {
            beginX=blockCount-1;
        }
        for(int y=0; y < blockCount; y ++) {
            if(slidePushLine(beginX, y, gapX, 0))
                res = true;
        }
        return res;
    }

    boolean slidePushVtc(JGameLib.DirType dir) {
        boolean res = false;
        int gapY=-1, beginY=0;
        if(dir == JGameLib.DirType.UP) {
            gapY=1;
        } else if(dir == JGameLib.DirType.DOWN) {
            beginY=blockCount-1;
        }
        for(int x=0; x < blockCount; x ++) {
            if(slidePushLine(x, beginY, 0, gapY))
                res = true;
        }
        return res;
    }

    boolean slidePushLine(int beginX, int beginY, int gapX, int gapY) {
        boolean res = false;
        int x = beginX, y = beginY;
        ArrayList<Integer> nums = new ArrayList();
        boolean merge = false;
        for(int j=0; j < blockCount; j ++) {
            if(numCards[y][x].isTextEmpty()) {
                x += gapX;
                y += gapY;
                continue;
            }
            int n = numCards[y][x].text2int();
            numCards[y][x].text("");
            numCards[y][x].backColor(colorEmpty);
            if(!merge && !nums.isEmpty() && nums.get(nums.size()-1) == n) {
                nums.set(nums.size()-1, n*2);
                merge = true;
                res = true;
            } else {
                if(j != nums.size())
                    res = true;
                nums.add(n);
            }
            x += gapX;
            y += gapY;
        }
        x = beginX;
        y = beginY;
        for(int i=0; i < nums.size(); i++) {
            numCards[y][x].text(nums.get(i));
            numCards[y][x].backColor(color2back);
            x += gapX;
            y += gapY;
        }
        return res;
    }

    // User Event start ====================================

    public void onBtnRestart(View v) {
        restart();
    }

    // User Event end ====================================

    // Game Event start ====================================

    @Override
    public void onGameWorkEnded(JGameLib.Card card, JGameLib.WorkType workType) {}

    @Override
    public void onGameTouchEvent(JGameLib.Card card, int action, float x, float y) {
        switch(action) {
            case MotionEvent.ACTION_DOWN:
                touchX = x;
                touchY = y;
                break;
            case MotionEvent.ACTION_UP : {
                if(Math.abs(touchX - x) >= 1) {
                    JGameLib.DirType dir = JGameLib.DirType.LEFT;
                    if(touchX < x)
                        dir = JGameLib.DirType.RIGHT;
                    if(slidePushHrz(dir))
                        add2or4();
                } else if(Math.abs(touchY - y) >= 1) {
                    JGameLib.DirType dir = JGameLib.DirType.UP;
                    if(touchY < y)
                        dir = JGameLib.DirType.DOWN;
                    if(slidePushVtc(dir))
                        add2or4();
                }
                touchX = touchY = -1;
            }
        }
    }

    @Override
    public void onGameSensor(int sensorType, float x, float y, float z) {}

    @Override
    public void onGameCollision(JGameLib.Card card1, JGameLib.Card card2) {}

    @Override
    public void onGameTimer() {}

    // Game Event end ====================================

}