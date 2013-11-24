"/usr/texbin/pdflatex" ausarbeitung
"/usr/texbin/bibtex" ausarbeitung.aux
"/usr/texbin/makeindex" ausarbeitung.tex
"/usr/texbin/makeglossaries" ausarbeitung
"/usr/texbin/pdflatex" ausarbeitung

latexmk -pvc -pdf -bibtex ausarbeitung
