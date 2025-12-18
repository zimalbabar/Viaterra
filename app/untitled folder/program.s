        .section .vectors, "a", %progbits
        .word 0x20002000        
        .word Reset_Handler    

        .section .text
        .global Reset_Handler

SYS_WRITINT  = 0x04          
Reset_Handler:

        SUB SP, SP, #20         
        MOV R0, #0              
init_loop:
        CMP R0, #4
        BGE end_init
        LSL R1, R0, #2          
        MOV R2, SP
        ADD R2, R2, R1
        MOV R3, #0
        STR R3, [R2]
        ADD R0, R0, #1
        B init_loop
end_init:

        SUB R0, R0, #1          

        
        ADD R1, SP, #16
        STR R0, [R1]

    
        MOV R4, #0              
print_loop:
        CMP R4, #4
        BGE print_i
        LSL R1, R4, #2
        MOV R2, SP
        ADD R2, R2, R1
        LDR R0, [R2]            
        MOV R1, #SYS_WRITINT
        BKPT 0xAB                
        MOV R0, #' '           
        MOV R1, #0x05
        BKPT 0xAB
        ADD R4, R4, #1
        B print_loop

print_i:
        MOV R0, #0x0A
        MOV R1, #0x05
        BKPT 0xAB


        LDR R0, =str_i
        BL print_string

        LDR R0, [SP, #16]
        MOV R1, #SYS_WRITINT
        BKPT 0xAB

        MOV R0, #0x0A
        MOV R1, #0x05
        BKPT 0xAB

        B .                     

print_string:
        LDRB R3, [R0], #1
        CMP R3, #0
        BEQ end_print_string
        MOV R1, #0x05
        BKPT 0xAB
        B print_string
end_print_string:
        BX LR

        .section .rodata
str_i:
        .asciz "i = "

        .end
