#!/bin/bash

JAR_NAME="java-chat.jar"
LOG_FILE="java-chat.log"
PID_FILE="java-chat.pid"
PORTA="12345"

# Verifica se o jar existe
if [ ! -f "$JAR_NAME" ]; then
    printf -v date '%(%Y-%m-%d %H:%M:%S)T' -1
    echo "[$date] Erro: $JAR_NAME nao encontrado!"
    exit 1
fi

# Verifica se já existe um processo rodando
if [ -f "$PID_FILE" ]; then
    PID=$(cat $PID_FILE)
    if kill -0 $PID 2>/dev/null; then
        printf -v date '%(%Y-%m-%d %H:%M:%S)T' -1
        echo "[$date] Erro: aplicacao ja esta rodando com PID $PID!"
        exit 1
    else
        rm -f $PID_FILE
    fi
fi

# Executa o jar e salva o log
printf -v date '%(%Y-%m-%d %H:%M:%S)T' -1
echo "[$date] Iniciando $JAR_NAME..." | tee -a $LOG_FILE
printf -v date '%(%Y-%m-%d %H:%M:%S)T' -1
echo "[$date] java -jar $JAR_NAME -s -p=$PORTA >> $LOG_FILE" | tee -a $LOG_FILE

nohup java -jar $JAR_NAME -s -p=$PORTA >> $LOG_FILE 2>&1 &

# Salva o PID no arquivo
echo $! > $PID_FILE

printf -v date '%(%Y-%m-%d %H:%M:%S)T' -1
echo "[$date] Aplicacao iniciada! PID: $!" | tee -a $LOG_FILE
echo "Logs em: $LOG_FILE"
echo "PID salvo em: $PID_FILE"