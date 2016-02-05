" Vim syntax file
" Language: Log Check Log File
" Maintainer: Kervin Pierre
" Latest Revision: 2016-01-20

if exists("b:current_syntax")
  finish
endif

syntax keyword lcLine tailerThread doHandle Expected "Pass Count"

highlight link keyword lcLine


