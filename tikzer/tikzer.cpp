#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include <unistd.h>
#include <string>

using namespace std;

int n;
struct Node
{
    int id;
    char sis, aua;
};

int idd;
char ss[20], aa[20];

Node nodes[1001];
double mat[1001][1001];

inline void gen_tikz()
{
    FILE *g = fopen("ret.tex", "w");
    fprintf(g, "\\documentclass[crop,tikz]{standalone}\n");
    fprintf(g, "\\definecolor{mygreen}{HTML}{006400}\n");
    fprintf(g, "\\definecolor{myred}{HTML}{8B0000}\n");
    fprintf(g, "\\begin{document}\n");
    fprintf(g, "\\begin{tikzpicture}[transform shape,line width=2pt]\n");
    double R = 1.0;
    while (2.0 * R * sin(M_PI/n) < 3) R += 0.1;
    for (int i=0;i<n;i++)
    {
        double pos = (i * 360.0) / (n * 1.0);
        string color = "white";
        if (nodes[i].sis == 'S')
        {
            color = "blue";
        } 
        if (nodes[i].sis == 'I')
        {
            color = "myred";
        }
        else if (nodes[i].sis == 'V')
        {
            color = "mygreen";
        }
        fprintf(g, "\\node[draw,circle,inner sep=0.25cm,fill=%s,text=white] (N-%d) at (%lf:%lfcm) [thick] {$%c$};\n", color.c_str(), i, pos, R, nodes[i].aua);
    }
    for (int i=0;i<n;i++)
    {
        for (int j=0;j<i;j++)
        {
            double w = fmax(0.01, mat[i][j]);
            fprintf(g, "\\path(N-%d) edge[-, red, opacity=%lf] (N-%d);\n", i, w, j);
        }
    }
    fprintf(g, "\\end{tikzpicture}\n");
    fprintf(g, "\\end{document}\n");
    fclose(g);
}

inline void build_tikz()
{
    system("exec pdflatex ret.tex &> /dev/null");
    system("exec convert -density 300 ret.pdf -quality 90 ret.png &> /dev/null");
}

int main()
{
    while (true)
    {
        FILE *f = fopen("final_log.log", "r");
        if (f == NULL)
        {
            usleep(5000000);
            continue;
        }
        fscanf(f, "%*s");
        fscanf(f, "%d%*d", &n);
        for (int i=0;i<n;i++)
        {
            fscanf(f, "%d%s%s", &idd, aa, ss);
            nodes[i].id = idd;
            nodes[i].sis = ss[0];
            nodes[i].aua = aa[0];
        }
        for (int i=0;i<n;i++)
        {
            double sum = 0.0;
            for (int j=0;j<n;j++)
            {
                fscanf(f, "%lf", &mat[i][j]);
                sum += mat[i][j];
            }
            if (sum > 0.0)
            {
                for (int j=0;j<n;j++)
                {
                    mat[i][j] /= sum;
                }
            }
        }
        printf("Data processed.\n");
        gen_tikz();
        printf("TikZ generated.\n");
        build_tikz();
        printf("TikZ built.\n");
        fclose(f);
        usleep(5000000);
    }
    return 0;
}
