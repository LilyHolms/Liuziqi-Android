package com.softwareprojectmanagement.liuziqi.jelly;

import android.os.SystemClock;

import java.util.Arrays;

/**
 * Created by 峻瑶 on 2016/4/13.
 */

public class Connect6AI {
    private int BLANK = 0;//没有棋子
    private int BLACK = 1;//黑棋子
    private int WHITE = 2;//白棋子
    private int INF=9999999;
    private int BOARDSIZE = 19;//棋盘大小
    private int dir[][]={{1,0},{1,1},{0,1},{1,-1},{-1,0},{-1,-1},{0,-1},{-1,1}};
    private int map[][]=new int[BOARDSIZE][BOARDSIZE];
    private int myColor;
    private int nowstep=0;

    private int roadScore[]={0,20,80,150,800,1000,9000000};
    private int whiteRoadSum[]=new int[7];
    private int blackRoadSum[]=new int[7];
    private int val[]=new int[3];

    private int level;
    private int searchDepth=2;

    private int killVal[][][][]=new int[20][20][20][20];

    class pointStack{
        public int len;
        public int x[];
        public int y[];
        pointStack()
        {
            x=new int[400];
            y=new int[400];
        }

    }

    class moveStack{
        public int len;
        public move allMove[];
    }

    private moveStack myStack[]=new moveStack[5];
    private pointStack myPoints[]=new pointStack[5];

    public void setMyColor(int color)
    {
        myColor=color;
    }

    public void setLevel(int l)
    {
        if(l==1)
        {
            level=2;
            searchDepth=1;
        }
        else if(l==2)
        {
            level=3;
            searchDepth=1;
        }
        else if(l==3)
        {
            level=2;
            searchDepth=2;
        }
    }

    //执行招法
    public void makeMove(move next,int color)
    {
        for(int i=0;i<next.len;i++)
        {
            map[next.x[i]][next.y[i]]=color;
        }
        nowstep++;
    }

    //撤销招法
    public void unMakeMove(move next)
    {
        for(int i=0;i<next.len;i++)
        {
            map[next.x[i]][next.y[i]]=0;
        }
        nowstep--;
    }

    //生成所有招法
    private void createMove(int depth,int color)
    {
        int k=0;
        myPoints[depth]=new pointStack();
        myStack[depth]=new moveStack();
        for(int i=0;i<BOARDSIZE;i++)
        {
            for(int j=0;j<BOARDSIZE;j++)
            {
                if(selMove(i,j) && map[i][j]==BLANK)
                {
                    myPoints[depth].x[k]=i;
                    myPoints[depth].y[k]=j;
                    k++;
                }
            }
        }
        int l=(k-1)*k/2;
        myStack[depth].allMove=new move[l];
        myStack[depth].len=l;
        int s=0;
        for(int i=0;i<k;i++) {
            for (int j = i + 1; j < k; j++) {
                myStack[depth].allMove[s] = new move();
                myStack[depth].allMove[s].x[0] = myPoints[depth].x[i];
                myStack[depth].allMove[s].y[0] = myPoints[depth].y[i];
                myStack[depth].allMove[s].x[1] = myPoints[depth].x[j];
                myStack[depth].allMove[s].y[1] = myPoints[depth].y[j];
                myStack[depth].allMove[s].len = 2;
                s++;
            }
        }
    }

    //可行落子点判断
    private boolean selMove(int x,int y)
    {
        int dotx,doty;
        int temp=level;
        if(nowstep==1)
        {
            level=1;
        }
        for(int i=-level;i<=level;i++)
        {
            for(int j=-level;j<=level;j++)
            {
                dotx=x+i;
                doty=y+j;
                if(inBoard(dotx,doty) && map[dotx][doty]!=BLANK)
                {
                    if(level==1)
                    {
                        level=temp;
                    }
                    return true;
                }
            }
        }
        if(level==1)
        {
            level=temp;
        }
        return false;
    }

    private boolean inBoard(int x,int y)
    {
        if(x>=0 && x<BOARDSIZE && y>=0 && y<BOARDSIZE)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //基于"路"的简易估值
    int Evaluate(int color)
    {
        val[WHITE]=0;
        val[BLACK]=0;
        for(int i=0;i<7;i++)
        {
            whiteRoadSum[i]=0;
            blackRoadSum[i]=0;
        }

        int whiteSum=0;
        int blackSum=0;
        int nextX,nextY;
        for(int i=0;i<BOARDSIZE;i++)
        {
            for(int j=0;j<BOARDSIZE;j++)
            {
                for(int k=0;k<4;k++)
                {
                    if(inBoard(i+5*dir[k][0],j+5*dir[k][1]))
                    {
                        whiteSum=0;
                        blackSum=0;
                        for(int l=0;l<6;l++)
                        {
                            nextX=i+l*dir[k][0];
                            nextY=j+l*dir[k][1];
                            if(map[nextX][nextY]==WHITE)
                            {
                                whiteSum++;
                            }
                            else if(map[nextX][nextY]==BLACK)
                            {
                                blackSum++;
                            }
                        }
                        if(whiteSum==0)
                        {
                            blackRoadSum[blackSum]++;
                        }
                        else if(blackSum==0)
                        {
                            whiteRoadSum[whiteSum]++;
                        }
                    }
                }
            }
        }
        for(int i=1;i<7;i++)
        {
            val[WHITE]+=whiteRoadSum[i]*roadScore[i];
            val[BLACK]+=blackRoadSum[i]*roadScore[i];
        }
        if(color==WHITE)
        {
            return val[WHITE]-val[BLACK];
        }
        else
        {
            return val[BLACK]-val[WHITE];
        }
    }

    int AlphaBeta(int depth,int alpha,int beta,int color)
    {
        int val;
        if (depth == 0)
        {
            val = Evaluate(color);
            return val;
        }
        move bestMove=new move();
        move temp;
        int bestIndex=0,maxKill=-INF;
        createMove(depth, color);
        //杀手评分寻找排序
        for(int i=0;i<myStack[depth].len;i++)
        {
            myStack[depth].allMove[i].killVal = killVal[myStack[depth].allMove[i].x[0]][myStack[depth].allMove[i].y[0]][myStack[depth].allMove[i].x[1]][myStack[depth].allMove[i].y[1]];
        }
//        for(int i=0;i<myStack[depth].len;i++)
//        {
//            for(int j=i+1;j<myStack[depth].len;j++)
//            {
//                if(myStack[depth].allMove[i].killVal<myStack[depth].allMove[j].killVal)
//                {
//                    temp=myStack[depth].allMove[i];
//                    myStack[depth].allMove[i]=myStack[depth].allMove[j];
//                    myStack[depth].allMove[j]=temp;
//                }
//            }
//        }
        for(int i=0;i<myStack[depth].len;i++)
        {
            if(myStack[depth].allMove[i].killVal>maxKill)
            {
                maxKill=myStack[depth].allMove[i].killVal;
                bestMove=myStack[depth].allMove[i];
                bestIndex=i;
            }
        }
        myStack[depth].allMove[bestIndex]=myStack[depth].allMove[0];
        myStack[depth].allMove[0]=bestMove;

        myStack[depth].len=myStack[depth].len>100?100:myStack[depth].len;

        for (int i = 0; i < myStack[depth].len; i++)
        {
            makeMove(myStack[depth].allMove[i], color);
            val = -AlphaBeta(depth - 1, -beta, -alpha, color^3);
            unMakeMove(myStack[depth].allMove[i]);
            if (val >= beta)
            {
                bestMove=myStack[depth].allMove[i];
                killVal[bestMove.x[0]][bestMove.y[0]][bestMove.x[1]][bestMove.y[1]]+=2;
                return val;
            }
            if (val>alpha)
            {
                bestMove=myStack[depth].allMove[i];
                alpha = val;
            }
        }
        killVal[bestMove.x[0]][bestMove.y[0]][bestMove.x[1]][bestMove.y[1]]+=2;
        return alpha;
    }

    private move SearchBestMove(int depth,int alpha)
    {
//        Random rand=new Random();
//        int next=rand.nextInt(myStack.len);
//        return myStack.allMove[next];
        int val;
        long start= SystemClock.elapsedRealtime();
        move bestMove=new move();
        int bestIndex=0;
        createMove(depth,myColor);
        //先进行一层估值排序
        move temp;
        for(int i=0;i<myStack[depth].len;i++)
        {
            makeMove(myStack[depth].allMove[i],myColor);
            val=-AlphaBeta(0,-INF,-alpha,myColor^3);
            myStack[depth].allMove[i].val=val;
            unMakeMove(myStack[depth].allMove[i]);
            if(val>alpha)
            {
                alpha=val;
                bestMove=myStack[depth].allMove[i];
                bestIndex=i;
            }
        }
        if(searchDepth==1)
        {
            return bestMove;
        }
        for(int i=0;i<myStack[depth].len;i++)
        {
            for(int j=i+1;j<myStack[depth].len;j++)
            {
                if(myStack[depth].allMove[i].val<myStack[depth].allMove[j].val)
                {
                    temp=myStack[depth].allMove[i];
                    myStack[depth].allMove[i]=myStack[depth].allMove[j];
                    myStack[depth].allMove[j]=temp;
                }
            }
        }
        myStack[depth].len=myStack[depth].len>30?30:myStack[depth].len;
//        myStack[depth].allMove[bestIndex]=myStack[depth].allMove[0];
//        myStack[depth].allMove[0]=bestMove;
        alpha=-INF;
        int best=-1;
        for(int i=0;i<myStack[depth].len;i++)
        {
            makeMove(myStack[depth].allMove[i],myColor);
            val=-AlphaBeta(depth-1,-INF,-alpha,myColor^3);
            unMakeMove(myStack[depth].allMove[i]);
            if(val>alpha)
            {
                best=i;
                alpha=val;
                bestMove=myStack[depth].allMove[i];
            }
        }
        System.out.println(">>>>bestMove:"+best);
        return bestMove;
    }

    public move getNextMove()
    {
        move bestMove =new move();
        if(nowstep==0 && myColor==BLACK)
        {
            bestMove.len=1;
            bestMove.x[0]=9;
            bestMove.y[0]=9;
            makeMove(bestMove,myColor);
            return bestMove;
        }
        bestMove=SearchBestMove(searchDepth,-INF);
        makeMove(bestMove,myColor);
        return bestMove;
    }
}