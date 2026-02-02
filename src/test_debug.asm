.data
nl: .asciiz "\n"
flt_0: .float 2.0
flt_1: .float 1.0
str_0: .asciiz "== FLOAT: DIV / INC / RETURN =="
flt_3: .float 5.0
str_1: .asciiz "f"
str_2: .asciiz "f/2.0"
flt_4: .float 2.0
str_3: .asciiz "++f"
flt_5: .float 1.0
str_4: .asciiz "f (despues de ++f)"
str_5: .asciiz "retF(3.0)"
flt_6: .float 3.0
str_6: .asciiz "poly(3.0) = x^2+2x+1"
flt_7: .float 3.0
str_7: .asciiz "== RELACIONALES =="
str_8: .asciiz "3 < 5"
str_9: .asciiz "3 <= 3"
str_10: .asciiz "5 > 9"
str_11: .asciiz "5 != 9"
str_12: .asciiz "== LOGICAS =="
str_13: .asciiz "b1"
str_14: .asciiz "b2"
str_15: .asciiz "b1 @ b2"
str_16: .asciiz "b1 ~ b2"
str_17: .asciiz "NOT b1 (using Sigma)"
str_18: .asciiz "== POWER =="
str_19: .asciiz "2 ^ 3"

.text
.globl main


retF:
    addi $sp, $sp, -4
    sw $fp, 0($sp)
    move $fp, $sp
    addi $sp, $sp, -4
    sw $ra, 0($sp)
    addi $sp, $sp, -0
    l.s $f0, 4($fp)
    j exit_func_retF
exit_func_retF:
    lw $ra, -4($fp)
    move $sp, $fp
    lw $fp, 0($sp)
    addi $sp, $sp, 4
    jr $ra

poly:
    addi $sp, $sp, -4
    sw $fp, 0($sp)
    move $fp, $sp
    addi $sp, $sp, -4
    sw $ra, 0($sp)
    addi $sp, $sp, -0
    l.s $f0, 4($fp)
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    l.s $f0, 4($fp)
    mov.s $f1, $f0
    lwc1 $f0, 0($sp)
    addi $sp, $sp, 4
    mul.s $f0, $f0, $f1
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    l.s $f0, flt_0
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    l.s $f0, 4($fp)
    mov.s $f1, $f0
    lwc1 $f0, 0($sp)
    addi $sp, $sp, 4
    mul.s $f0, $f0, $f1
    mov.s $f1, $f0
    lwc1 $f0, 0($sp)
    addi $sp, $sp, 4
    add.s $f0, $f0, $f1
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    l.s $f0, flt_1
    mov.s $f1, $f0
    lwc1 $f0, 0($sp)
    addi $sp, $sp, 4
    add.s $f0, $f0, $f1
    j exit_func_poly
exit_func_poly:
    lw $ra, -4($fp)
    move $sp, $fp
    lw $fp, 0($sp)
    addi $sp, $sp, 4
    jr $ra
main:
    addi $sp, $sp, -4
    sw $fp, 0($sp)
    move $fp, $sp
    addi $sp, $sp, -4
    sw $ra, 0($sp)
    addi $sp, $sp, -16
    li $v0, 4
    la $a0, str_0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, flt_3
    s.s $f0, -8($fp)
    li $v0, 4
    la $a0, str_1
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, -8($fp)
    li $v0, 2
    mov.s $f12, $f0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $v0, 4
    la $a0, str_2
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, -8($fp)
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    l.s $f0, flt_4
    mov.s $f1, $f0
    lwc1 $f0, 0($sp)
    addi $sp, $sp, 4
    div.s $f0, $f0, $f1
    li $v0, 2
    mov.s $f12, $f0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $v0, 4
    la $a0, str_3
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, -8($fp)
    addi $t1, $fp, -8
    l.s $f0, 0($t1)
    l.s $f1, flt_5
    add.s $f0, $f0, $f1
    s.s $f0, 0($t1)
    li $v0, 2
    mov.s $f12, $f0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $v0, 4
    la $a0, str_4
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, -8($fp)
    li $v0, 2
    mov.s $f12, $f0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $v0, 4
    la $a0, str_5
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, flt_6
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    jal retF
    addi $sp, $sp, 4
    li $v0, 2
    mov.s $f12, $f0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $v0, 4
    la $a0, str_6
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, flt_7
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    jal poly
    addi $sp, $sp, 4
    li $v0, 2
    mov.s $f12, $f0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $v0, 4
    la $a0, str_7
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $v0, 4
    la $a0, str_8
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 3
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 5
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t0, $t1
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $v0, 4
    la $a0, str_9
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 3
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 3
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    sle $t0, $t0, $t1
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $v0, 4
    la $a0, str_10
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 5
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 9
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    sgt $t0, $t0, $t1
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $v0, 4
    la $a0, str_11
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 5
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 9
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    sne $t0, $t0, $t1
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $v0, 4
    la $a0, str_12
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 1
    sw $t0, -12($fp)
    li $t0, 0
    sw $t0, -16($fp)
    li $v0, 4
    la $a0, str_13
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -12($fp)
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $v0, 4
    la $a0, str_14
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -16($fp)
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $v0, 4
    la $a0, str_15
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -12($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    lw $t0, -16($fp)
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    lw $t0, -12($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    lw $t0, -16($fp)
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    lw $t0, -12($fp)
    beq $t0, $zero, bool_f_9
    j and_rhs_11
and_rhs_11:
    lw $t0, -16($fp)
    beq $t0, $zero, bool_f_9
    j bool_t_8
bool_f_9:
    li $t0, 0
    j bool_end_10
bool_t_8:
    li $t0, 1
bool_end_10:
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $v0, 4
    la $a0, str_16
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -12($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    lw $t0, -16($fp)
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    lw $t0, -12($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    lw $t0, -16($fp)
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    lw $t0, -12($fp)
    beq $t0, $zero, or_rhs_15
    j bool_t_12
or_rhs_15:
    lw $t0, -16($fp)
    beq $t0, $zero, bool_f_13
    j bool_t_12
bool_f_13:
    li $t0, 0
    j bool_end_14
bool_t_12:
    li $t0, 1
bool_end_14:
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $v0, 4
    la $a0, str_17
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -12($fp)
    xori $t0, $t0, 1
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $v0, 4
    la $a0, str_18
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $v0, 4
    la $a0, str_19
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 2
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 3
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    li $t2, 1
pow_loop_16:
    beq $t1, $zero, pow_end_17
    mul $t2, $t2, $t0
    addi $t1, $t1, -1
    j pow_loop_16
pow_end_17:
    move $t0, $t2
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
exit_main_2:
    lw $ra, -4($fp)
    move $sp, $fp
    lw $fp, 0($sp)
    addi $sp, $sp, 4
    li $v0, 10
    syscall
