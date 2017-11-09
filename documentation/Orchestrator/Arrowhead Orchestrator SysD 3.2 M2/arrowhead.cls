% arrowhead.cls
%
% A LaTeX document class for Arrowhead documentation.
\def\fileversion{1.0}
\def\filedate{2017/08/31}
\NeedsTeXFormat{LaTeX2e}

\ProvidesClass{arrowhead}[\filedate\space Version \fileversion\space]
\LoadClass{article}

\RequirePackage{xparse}
\RequirePackage{couriers}
\RequirePackage{ifthen}
\RequirePackage[hmargin=2cm,top=4cm,headheight=4cm,bottom=3cm]{geometry}
\RequirePackage{fancyhdr}
\RequirePackage{helvet}
\RequirePackage{lastpage}
\RequirePackage{listings}
\RequirePackage{microtype}
\RequirePackage[pdftex]{graphicx}
\RequirePackage{tabularx}
\RequirePackage[font={small}]{caption}

\DeclareOption*{\PassOptionsToClass{\CurrentOption}{article}}

\ProcessOptions\relax

\NewDocumentCommand{\ArrowheadTitle}{m}{\newcommand{\arrowtitle}{#1}}
\NewDocumentCommand{\ArrowheadType}{m}{\newcommand{\arrowtype}{#1}}
\NewDocumentCommand{\ArrowheadVersion}{m}{\newcommand{\arrowversion}{#1}}
\NewDocumentCommand{\ArrowheadDate}{m}{\newcommand{\arrowdate}{#1}}
\NewDocumentCommand{\ArrowheadAuthor}{m}{\newcommand{\arrowauthor}{#1}}
\NewDocumentCommand{\ArrowheadStatus}{m}{\newcommand{\arrowstatus}{#1}}
\NewDocumentCommand{\ArrowheadContact}{m}{\newcommand{\arrowcontact}{#1}}
\NewDocumentCommand{\ArrowheadFooter}{m}{\newcommand{\arrowfooter}{#1}}

\makeatletter
\renewcommand\tiny{\@setfontsize\tiny{6}{7}}
\renewenvironment{abstract}{%
  \begin{flushleft}
    {\large \textbf{\abstractname}}\\
    \normalsize
}{%
  \end{flushleft}
}

\makeatother
\setlength\tabcolsep{1mm}
\renewcommand\arraystretch{1.3}
\renewcommand{\familydefault}{\sfdefault}

\renewcommand*{\headrulewidth}{0pt}
\pagestyle{fancy}

\newcounter{Table}
\renewcommand\theTable{\arabic{Table}}

\fancyhf{}
%% Page Header
\lhead{
  \raisebox{-0.5\height}{\includegraphics[height=2.33cm]{fig/arrowhead_logo}}
}
\rhead{%
  \renewcommand{\arraystretch}{0.75}%
  \begin{tabular}{p{8.67cm} p{3.33cm}}
    \ifthenelse{\value{page}=1}{%
      % First Page Header
      \tiny{Document title} & \tiny{Document type} \\
      \footnotesize{\textbf{\arrowtitle}} & \footnotesize{\textbf{\arrowtype}} \\
      \tiny{Date} & \tiny{Version} \\
      \footnotesize{\textbf{\arrowdate}} & \footnotesize{\textbf{\arrowversion}} \\
      \tiny{Author} & \tiny{Status} \\
      \footnotesize{\textbf{\arrowauthor}} & \footnotesize{\textbf{\arrowstatus}} \\
      \tiny{Contact} & \tiny{Page} \\
      \footnotesize{\textbf{\arrowcontact}} & \footnotesize{\textbf{\thepage \ (\pageref{LastPage})}}
    }{%
      % Standard Page Header
      \tiny{Document title} & \tiny{Version} \\
      \footnotesize{\textbf{\arrowtitle}} & \footnotesize{\textbf{\arrowversion}} \\
      \tiny{Date} & \tiny{Status} \\
      \footnotesize{\textbf{\arrowdate}} & \footnotesize{\textbf{\arrowstatus}} \\
      & \tiny{Page} \\
      & \footnotesize{\textbf{\thepage \ (\pageref{LastPage})}}
    }
  \end{tabular}%
}
%%

%% Page Footer
\lfoot{
  \scriptsize
  \begin{tabularx}{\textwidth}{l X}
    & \\ \hline
    \arrowfooter & \\
  \end{tabularx}
}
%%

\lstdefinelanguage{HTTP}{
  morekeywords={CONNECT,DELETE,GET,HEAD,OPTIONS,PUT,POST,PATCH,HTTP,HTTPS},
  sensitive=false,
  morecomment=[l]{//},
  morecomment=[s]{/*}{*/}, % s is for start and end delimiter
  morestring=[b]"
}

\lstset{%
  language=HTTP,
  backgroundcolor=\color{white},
  basicstyle=\ttfamily\footnotesize,
  breakatwhitespace=false,
  breaklines=true,
  captionpos=b,
  commentstyle=\color{gray!20},
  extendedchars=true,
  frame=single,
  keepspaces=true,
  keywordstyle=\bfseries\color{gray},
  numbers=left,
  numbersep=1em,
  numberstyle=\tiny\color{gray!10},
  rulecolor=\color{gray!33},
  showspaces=false,
  showstringspaces=false,
  showtabs=false,
  stringstyle=\itshape\color{gray},
  tabsize=4,
}