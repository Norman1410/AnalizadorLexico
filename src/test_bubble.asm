.data
nl: .asciiz "\n"
.align 2
g_N: .word 8
.align 2
g_data: .word 0, 0, 0, 0, 0, 0, 0, 0
str_0: .asciiz "Ingrese 8 enteros:"
str_1: .asciiz "Antes:"
str_2: .asciiz "Despues:"

.text
.globl main

main:
    addi $sp, $sp, -4
    sw $fp, 0($sp)
    move $fp, $sp
    addi $sp, $sp, -4
    sw $ra, 0($sp)
    addi $sp, $sp, -24
    li $t0, 0
    sw $t0, -8($fp)
    li $t0, 0
    sw $t0, -12($fp)
    li $t0, 0
    sw $t0, -16($fp)
    li $t0, 0
    sw $t0, -20($fp)
    li $t0, 0
    sw $t0, -24($fp)
    la $t0, str_0
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 0
    sw $t0, -8($fp)
loop_start_1:
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    lw $t0, g_N
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t0, $t1
    beq $t0, $zero, decide_end_3
    addi $t1, $fp, -24
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $v0, 5
    syscall
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    sw $v0, 0($t1)
    la $t1, g_data
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t0, -8($fp)
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 8
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    sll $t2, $t2, 2
    addu $t1, $t1, $t2
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    lw $t0, -24($fp)
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    sw $t0, 0($t1)
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 1
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    add $t0, $t0, $t1
    sw $t0, -8($fp)
    j decide_end_3
decide_end_3:
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    lw $t0, g_N
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t0, $t1
    xori $t0, $t0, 1
    bne $t0, $zero, loop_end_2
    j loop_start_1
loop_end_2:
    la $t0, str_1
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 0
    sw $t0, -8($fp)
loop_start_4:
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    lw $t0, g_N
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t0, $t1
    beq $t0, $zero, decide_end_6
    la $t1, g_data
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t0, -8($fp)
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 8
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    sll $t2, $t2, 2
    addu $t1, $t1, $t2
    lw $t0, 0($t1)
    li $v0, 1
    move $a0, $t0
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
    add $t0, $t0, $t1
    sw $t0, -8($fp)
    j decide_end_6
decide_end_6:
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    lw $t0, g_N
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t0, $t1
    xori $t0, $t0, 1
    bne $t0, $zero, loop_end_5
    j loop_start_4
loop_end_5:
    li $t0, 0
    sw $t0, -8($fp)
loop_start_7:
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    lw $t0, g_N
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 1
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    sub $t0, $t0, $t1
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t0, $t1
    beq $t0, $zero, decide_end_9
    lw $t0, g_N
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 1
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    sub $t0, $t0, $t1
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    lw $t0, -8($fp)
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    sub $t0, $t0, $t1
    sw $t0, -20($fp)
    li $t0, 0
    sw $t0, -12($fp)
loop_start_10:
    lw $t0, -12($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    lw $t0, -20($fp)
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t0, $t1
    beq $t0, $zero, decide_end_12
    la $t1, g_data
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t0, -12($fp)
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 8
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    sll $t2, $t2, 2
    addu $t1, $t1, $t2
    lw $t0, 0($t1)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    la $t1, g_data
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t0, -12($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 1
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    add $t0, $t0, $t1
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 8
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    sll $t2, $t2, 2
    addu $t1, $t1, $t2
    lw $t0, 0($t1)
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t1, $t0
    beq $t0, $zero, decide_end_13
    la $t1, g_data
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t0, -12($fp)
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 8
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    sll $t2, $t2, 2
    addu $t1, $t1, $t2
    lw $t0, 0($t1)
    sw $t0, -16($fp)
    la $t1, g_data
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t0, -12($fp)
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 8
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    sll $t2, $t2, 2
    addu $t1, $t1, $t2
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    la $t1, g_data
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t0, -12($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 1
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    add $t0, $t0, $t1
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 8
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    sll $t2, $t2, 2
    addu $t1, $t1, $t2
    lw $t0, 0($t1)
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    sw $t0, 0($t1)
    la $t1, g_data
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t0, -12($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 1
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    add $t0, $t0, $t1
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 8
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    sll $t2, $t2, 2
    addu $t1, $t1, $t2
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    lw $t0, -16($fp)
    lw $t1, 0($sp)
    addi $sp, $sp, 4
    sw $t0, 0($t1)
    j decide_end_13
decide_end_13:
    lw $t0, -12($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 1
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    add $t0, $t0, $t1
    sw $t0, -12($fp)
    j decide_end_12
decide_end_12:
    lw $t0, -12($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    lw $t0, -20($fp)
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t0, $t1
    xori $t0, $t0, 1
    bne $t0, $zero, loop_end_11
    j loop_start_10
loop_end_11:
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 1
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    add $t0, $t0, $t1
    sw $t0, -8($fp)
    j decide_end_9
decide_end_9:
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    lw $t0, g_N
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 1
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    sub $t0, $t0, $t1
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t0, $t1
    xori $t0, $t0, 1
    bne $t0, $zero, loop_end_8
    j loop_start_7
loop_end_8:
    la $t0, str_2
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 0
    sw $t0, -8($fp)
loop_start_14:
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    lw $t0, g_N
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t0, $t1
    beq $t0, $zero, decide_end_16
    la $t1, g_data
    addi $sp, $sp, -4
    sw $t1, 0($sp)
    li $t0, 0
    move $t2, $t0
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t0, -8($fp)
    move $t3, $t0
    lw $t2, 0($sp)
    addi $sp, $sp, 4
    li $t4, 8
    mul $t2, $t2, $t4
    addu $t2, $t2, $t3
    addi $sp, $sp, -4
    sw $t2, 0($sp)
    lw $t1, 4($sp)
    lw $t2, 0($sp)
    addi $sp, $sp, 8
    sll $t2, $t2, 2
    addu $t1, $t1, $t2
    lw $t0, 0($t1)
    li $v0, 1
    move $a0, $t0
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
    add $t0, $t0, $t1
    sw $t0, -8($fp)
    j decide_end_16
decide_end_16:
    lw $t0, -8($fp)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    lw $t0, g_N
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t0, $t1
    xori $t0, $t0, 1
    bne $t0, $zero, loop_end_15
    j loop_start_14
loop_end_15:
exit_main_0:
    lw $ra, -4($fp)
    move $sp, $fp
    lw $fp, 0($sp)
    addi $sp, $sp, 4
    li $v0, 10
    syscall
