.data
nl: .asciiz "\n"
.align 2
str_0: .asciiz "=== CALCULADORA ==="
g_banner: .word str_0
flt_1: .float 0.0
flt_2: .float 0.0
flt_3: .float 0.0
str_1: .asciiz "Menu: 1=+, 2=-, 3=*, 4=/, 0=salir"
str_2: .asciiz "Operacion:"
str_3: .asciiz "a (float):"
str_4: .asciiz "b (float):"
str_5: .asciiz "Debug b:"
str_6: .asciiz "a + b ="
str_7: .asciiz "a - b ="
str_8: .asciiz "a * b ="
flt_15: .float 0.0
str_9: .asciiz "a / b ="
str_10: .asciiz "Error: division por cero"
str_11: .asciiz "Opcion invalida"
str_12: .asciiz "op es par? (bool):"

.text
.globl main


isEven:
    addi $sp, $sp, -4
    sw $fp, 0($sp)
    move $fp, $sp
    addi $sp, $sp, -4
    sw $ra, 0($sp)
    addi $sp, $sp, -0
    lw $t0, 4($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 2
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    div $t0, $t1
    mfhi $t0
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 0
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    xor $t0, $t0, $t1
    sltiu $t0, $t0, 1
    move $v0, $t0
    j exit_func_isEven
exit_func_isEven:
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
    addi $sp, $sp, -24
    li $t0, 0
    sw $t0, -8($fp)
    l.s $f0, flt_1
    s.s $f0, -12($fp)
    l.s $f0, flt_2
    s.s $f0, -16($fp)
    l.s $f0, flt_3
    s.s $f0, -20($fp)
    li $t0, 0
    sw $t0, -24($fp)
    li $t0, 0
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
loop_start_4:
    la $t0, str_1
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_2
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    addi $t1, $fp, -8
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $v0, 5
    syscall
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    sw $v0, 0($t1)
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 0
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    xor $t0, $t0, $t1
    sltu $t0, $zero, $t0
    beq $t0, $zero, decide_next_7
    la $t0, str_3
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    addi $t1, $fp, -12
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $v0, 6
    syscall
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    s.s $f0, 0($t1)
    la $t0, str_4
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    addi $t1, $fp, -16
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $v0, 6
    syscall
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    s.s $f0, 0($t1)
    la $t0, str_5
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, -16($fp)
    mov.s $f12, $f0
    li $v0, 2
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 1
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    xor $t0, $t0, $t1
    sltiu $t0, $t0, 1
    beq $t0, $zero, decide_next_9
    l.s $f0, -12($fp)
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    l.s $f0, -16($fp)
    mov.s $f1, $f0
    lwc1 $f0, 0($sp)
    addi $sp, $sp, 4
    add.s $f0, $f0, $f1
    s.s $f0, -20($fp)
    la $t0, str_6
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, -20($fp)
    mov.s $f12, $f0
    li $v0, 2
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    j decide_end_8
decide_next_9:
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 2
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    xor $t0, $t0, $t1
    sltiu $t0, $t0, 1
    beq $t0, $zero, decide_next_10
    l.s $f0, -12($fp)
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    l.s $f0, -16($fp)
    mov.s $f1, $f0
    lwc1 $f0, 0($sp)
    addi $sp, $sp, 4
    sub.s $f0, $f0, $f1
    s.s $f0, -20($fp)
    la $t0, str_7
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, -20($fp)
    mov.s $f12, $f0
    li $v0, 2
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    j decide_end_8
decide_next_10:
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 3
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    xor $t0, $t0, $t1
    sltiu $t0, $t0, 1
    beq $t0, $zero, decide_next_11
    l.s $f0, -12($fp)
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    l.s $f0, -16($fp)
    mov.s $f1, $f0
    lwc1 $f0, 0($sp)
    addi $sp, $sp, 4
    mul.s $f0, $f0, $f1
    s.s $f0, -20($fp)
    la $t0, str_8
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, -20($fp)
    mov.s $f12, $f0
    li $v0, 2
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    j decide_end_8
decide_next_11:
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 4
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    xor $t0, $t0, $t1
    sltiu $t0, $t0, 1
    beq $t0, $zero, decide_next_12
    l.s $f0, -16($fp)
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    l.s $f0, flt_15
    mov.s $f1, $f0
    lwc1 $f0, 0($sp)
    addi $sp, $sp, 4
    c.eq.s $f0, $f1
    bc1f flt_rel_e_16
    li $t0, 0
    j flt_rel_f_17
flt_rel_e_16:
    li $t0, 1
flt_rel_f_17:
    beq $t0, $zero, decide_next_14
    l.s $f0, -12($fp)
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    l.s $f0, -16($fp)
    mov.s $f1, $f0
    lwc1 $f0, 0($sp)
    addi $sp, $sp, 4
    div.s $f0, $f0, $f1
    s.s $f0, -20($fp)
    la $t0, str_9
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, -20($fp)
    mov.s $f12, $f0
    li $v0, 2
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    j decide_end_13
decide_next_14:
    la $t0, str_10
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
decide_end_13:
    j decide_end_8
decide_next_12:
    la $t0, str_11
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
decide_end_8:
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    jal isEven
    addi $sp, $sp, 4
    move $t0, $v0
    sw $t0, -24($fp)
    la $t0, str_12
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -24($fp)
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    j decide_end_6
decide_next_7:
decide_end_6:
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 0
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    xor $t0, $t0, $t1
    sltiu $t0, $t0, 1
    bne $t0, $zero, loop_end_5
    j loop_start_4
    j loop_start_4
loop_end_5:
exit_main_0:
    lw $ra, -4($fp)
    move $sp, $fp
    lw $fp, 0($sp)
    addi $sp, $sp, 4
    li $v0, 10
    syscall
