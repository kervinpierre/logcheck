" Vim syntax file
" Language: Log Check Log File
" Maintainer: Kervin Pierre
" Latest Revision: 2016-01-20

if exists("b:current_syntax")
  finish
endif

syntax keyword lcKeyword tailerThread doHandle Expected
syntax keyword lcFunction runLogStoreThread doProcess
"syntax keyword lcOperator 
syntax keyword lcString WARN ERROR REOPEN INTERRUPTED VALIDATION_FAIL VALIDATION_ERROR
syntax keyword lcComment PassCount TailerStart
"syntax keyword lcStatement

highlight link lcKeyword Keyword
highlight link lcFunction Function
highlight link lcOperator Operator
highlight link lcComment Comment
highlight link lcStatement Statement
highlight link lcString String

set number

" use the command below to use this syntax
" :set syntax=logcheck
