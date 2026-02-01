.data
nl: .asciiz "\n"
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
    addi $sp, $sp, -8
    sw $ra, 4($sp)
    sw $s0, 0($sp)
    addi $sp, $sp, -4
    move $s0, $sp
    lw $t0, 12($sp)
    sw $t0, 0($s0)
    lw $t0, 0($s0)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 2
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    div $t1, $t0
    mfhi $t0
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 0
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    sub $t0, $t0, $t1
    sltiu $t0, $t0, 1
    move $v0, $t0
    j exit_func_isEven
exit_func_isEven:
    addi $sp, $sp, 4
    lw $s0, 0($sp)
    lw $ra, 4($sp)
    addi $sp, $sp, 8
    jr $ra
main:
    addi $sp, $sp, -20
    move $s0, $sp
    li $t0, 0
    sw $t0, 0($s0)
    l.s $f0, flt_1
    s.s $f0, 4($s0)
    l.s $f0, flt_2
    s.s $f0, 8($s0)
    l.s $f0, flt_3
    s.s $f0, 12($s0)
    li $t0, 0
    sw $t0, 16($s0)
    lw $t0, g_banner
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
loop_start_4:
    li $v0, 4
    la $a0, str_1
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
    addi $t1, $s0, 0
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $v0, 5
    syscall
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    sw $v0, 0($t1)
    lw $t0, 0($s0)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 0
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    beq $t1, $t0, decide_next_7
    li $v0, 4
    la $a0, str_3
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    addi $t1, $s0, 4
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $v0, 6
    syscall
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    s.s $f0, 0($t1)
    li $v0, 4
    la $a0, str_4
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    addi $t1, $s0, 8
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $v0, 6
    syscall
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    s.s $f0, 0($t1)
    li $v0, 4
    la $a0, str_5
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, 8($s0)
    li $v0, 2
    mov.s $f12, $f0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, 0($s0)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 1
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    bne $t1, $t0, decide_next_9
    l.s $f0, 4($s0)
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    l.s $f0, 8($s0)
    mov.s $f1, $f0
    lwc1 $f0, 0($sp)
    addi $sp, $sp, 4
    add.s $f0, $f0, $f1
    s.s $f0, 12($s0)
    li $v0, 4
    la $a0, str_6
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, 12($s0)
    li $v0, 2
    mov.s $f12, $f0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    j decide_end_8
decide_next_9:
    lw $t0, 0($s0)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 2
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    bne $t1, $t0, decide_next_10
    l.s $f0, 4($s0)
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    l.s $f0, 8($s0)
    mov.s $f1, $f0
    lwc1 $f0, 0($sp)
    addi $sp, $sp, 4
    sub.s $f0, $f0, $f1
    s.s $f0, 12($s0)
    li $v0, 4
    la $a0, str_7
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, 12($s0)
    li $v0, 2
    mov.s $f12, $f0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    j decide_end_8
decide_next_10:
    lw $t0, 0($s0)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 3
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    bne $t1, $t0, decide_next_11
    l.s $f0, 4($s0)
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    l.s $f0, 8($s0)
    mov.s $f1, $f0
    lwc1 $f0, 0($sp)
    addi $sp, $sp, 4
    mul.s $f0, $f0, $f1
    s.s $f0, 12($s0)
    li $v0, 4
    la $a0, str_8
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, 12($s0)
    li $v0, 2
    mov.s $f12, $f0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    j decide_end_8
decide_next_11:
    lw $t0, 0($s0)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 4
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    bne $t1, $t0, decide_next_12
    l.s $f0, 8($s0)
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    l.s $f0, flt_15
    mov.s $f1, $f0
    lwc1 $f0, 0($sp)
    addi $sp, $sp, 4
    c.eq.s $f0, $f1
    bc1f flt_true_16
    li $t0, 0
    j flt_end_17
flt_true_16:
    li $t0, 1
flt_end_17:
    beq $t0, $zero, decide_next_14
    l.s $f0, 4($s0)
    addi $sp, $sp, -4
    swc1 $f0, 0($sp)
    l.s $f0, 8($s0)
    mov.s $f1, $f0
    lwc1 $f0, 0($sp)
    addi $sp, $sp, 4
    div.s $f0, $f0, $f1
    s.s $f0, 12($s0)
    li $v0, 4
    la $a0, str_9
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    l.s $f0, 12($s0)
    li $v0, 2
    mov.s $f12, $f0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    j decide_end_13
decide_next_14:
    li $v0, 4
    la $a0, str_10
    syscall
    li $v0, 4
    la $a0, nl
    syscall
decide_end_13:
    j decide_end_8
decide_next_12:
    li $v0, 4
    la $a0, str_11
    syscall
    li $v0, 4
    la $a0, nl
    syscall
decide_end_8:
    lw $t0, 0($s0)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    jal isEven
    addi $sp, $sp, 4
    move $t0, $v0
    sw $t0, 16($s0)
    li $v0, 4
    la $a0, str_12
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, 16($s0)
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    j decide_end_6
decide_next_7:
decide_end_6:
    lw $t0, 0($s0)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 0
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    beq $t1, $t0, loop_end_5
    j loop_start_4
    j loop_start_4
loop_end_5:
exit_main_0:
    addi $sp, $sp, 20
    li $v0, 10
    syscall
