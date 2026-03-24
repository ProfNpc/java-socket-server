#!/bin/bash

PID_FILE="java-chat.pid"
LOG_FILE="java-chat.log"

# Verifica se o arquivo de PID existe
if [ ! -f "$PID_FILE" ]; then
    printf -v date '%(%Y-%m-%d %H:%M:%S)T' -1
    echo "[$date] Erro: arquivo $PID_FILE nao encontrado. A aplicacao esta rodando?"
    exit 1
fi

PID=$(cat $PID_FILE)

# Verifica se o processo está rodando
if ! kill -0 $PID 2>/dev/null; then
    printf -v date '%(%Y-%m-%d %H:%M:%S)T' -1
    echo "[$date] Erro: processo com PID $PID nao esta rodando."
    rm -f $PID_FILE
    exit 1
fi

# Encerra o processo
printf -v date '%(%Y-%m-%d %H:%M:%S)T' -1
echo "[$date] Encerrando processo PID $PID..." | tee -a $LOG_FILE

kill $PID

# Aguarda o processo encerrar
for i in {1..10}; do
    if ! kill -0 $PID 2>/dev/null; then
        printf -v date '%(%Y-%m-%d %H:%M:%S)T' -1
        echo "[$date] Processo $PID encerrado com sucesso!" | tee -a $LOG_FILE
        rm -f $PID_FILE
        exit 0
    fi
    sleep 1
done

# Se ainda estiver rodando, força o encerramento
printf -v date '%(%Y-%m-%d %H:%M:%S)T' -1
echo "[$date] Processo nao encerrou, forcando encerramento (kill -9)..." | tee -a $LOG_FILE
kill -9 $PID
rm -f $PID_FILE

printf -v date '%(%Y-%m-%d %H:%M:%S)T' -1
echo "[$date] Processo $PID encerrado forcadamente!" | tee -a $LOG_FILE