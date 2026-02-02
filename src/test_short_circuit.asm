.data
nl: .asciiz "\n"
str_0: .asciiz "== TEST A: AND short-circuit =="
str_1: .asciiz "b1 @ (++x > 0) result:"
str_2: .asciiz "x value (should be 0):"
str_3: .asciiz "== TEST B: OR short-circuit =="
str_4: .asciiz "b2 ~ (++y > 0) result:"
str_5: .asciiz "y value (should be 0):"
str_6: .asciiz "== RELATIONAL PORTABILITY =="
str_7: .asciiz "3 <= 5 (slt + xori):"
str_8: .asciiz "5 >= 3 (slt + xori):"
str_9: .asciiz "5 == 5 (xor + sltiu):"
str_10: .asciiz "5 != 3 (xor + sltu):"

.text
.globl main

main:
    addi $sp, $sp, -4
    sw $fp, 0($sp)
    move $fp, $sp
    addi $sp, $sp, -4
    sw $ra, 0($sp)
    addi $sp, $sp, -16
    la $t0, str_0
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 0
    sw $t0, -8($fp)
    li $t0, 0
    sw $t0, -12($fp)
    la $t0, str_1
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -12($fp)
    beq $t0, $zero, logic_end_1
    lw $t0, -8($fp)
    addi $t1, $fp, -8
    lw $t0, 0($t1)
    addi $t0, $t0, 1
    sw $t0, 0($t1)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 0
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t1, $t0
logic_end_1:
    li $v0, 1
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
    lw $t0, -8($fp)
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_3
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 0
    sw $t0, -16($fp)
    li $t0, 1
    sw $t0, -20($fp)
    la $t0, str_4
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    lw $t0, -20($fp)
    bne $t0, $zero, logic_end_2
    lw $t0, -16($fp)
    addi $t1, $fp, -16
    lw $t0, 0($t1)
    addi $t0, $t0, 1
    sw $t0, 0($t1)
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 0
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t1, $t0
logic_end_2:
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_5
    li $v0, 4
    move $a0, $t0
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
    la $t0, str_6
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_7
    li $v0, 4
    move $a0, $t0
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
    slt $t0, $t1, $t0
    xori $t0, $t0, 1
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_8
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 5
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 3
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    slt $t0, $t0, $t1
    xori $t0, $t0, 1
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_9
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 5
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 5
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    xor $t0, $t0, $t1
    sltiu $t0, $t0, 1
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    la $t0, str_10
    li $v0, 4
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
    li $t0, 5
    addi $sp, $sp, -4
    sw $t0, 0($sp)
    li $t0, 3
    move $t1, $t0
    lw $t0, 0($sp)
    addi $sp, $sp, 4
    xor $t0, $t0, $t1
    sltu $t0, $zero, $t0
    li $v0, 1
    move $a0, $t0
    syscall
    li $v0, 4
    la $a0, nl
    syscall
exit_main_0:
    lw $ra, -4($fp)
    move $sp, $fp
    lw $fp, 0($sp)
    addi $sp, $sp, 4
    li $v0, 10
    syscall
