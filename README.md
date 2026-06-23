# Java Event Planner

Este projeto implementa a versão compacta de um sistema de agendamento e gerenciamento de eventos com interface gráfica (GUI), desenvolvido para a disciplina SCC0504 – Programação Orientada a Objetos.

## Principais Funcionalidades

* **Calendário mensal interativo:** Exibe os dias do mês em uma grade gerada automaticamente e destaca as datas que possuem eventos cadastrados por meio de um marcador "(*)" e de uma mudança na cor de fundo.
* **Gerenciamento completo de eventos (CRUD):** Permite criar, visualizar, editar e remover eventos utilizando janelas de diálogo (JDialog). O sistema também realiza validações para garantir que os dados informados sejam válidos, como impedir títulos vazios e verificar datas e horários.
* **Organização por categorias:** Os eventos podem ser classificados em categorias pré-definidas ('Meeting', 'Birthday', 'Appointment' e 'Other'), facilitando a organização dos compromissos.
* **Persistência de dados em arquivo:** Todos os eventos são salvos automaticamente em um arquivo de texto ('events_data.txt') e carregados novamente quando a aplicação é iniciada. O sistema também trata situações como a ausência do arquivo ou registros inválidos, evitando interrupções inesperadas.
* **Lembretes de eventos:** Ao iniciar a aplicação, é feita uma verificação dos eventos cadastrados para identificar aqueles cujo lembrete deve ser exibido no dia atual, mostrando uma notificação ao usuário.

## Conceitos de Programação Orientada a Objetos Utilizados

* **Encapsulamento:** Os atributos da classe 'Event' são privados e acessados por meio de métodos *getters* e *setters*, garantindo maior controle sobre os dados armazenados.
* **Sobrecarga e reutilização de construtores:** Foi utilizado o comando 'this(...)' para reaproveitar código entre construtores e evitar duplicação durante a criação dos objetos.
* **Estruturas de dados dinâmicas:** Os eventos são armazenados em coleções do tipo 'List' e 'ArrayList', permitindo adicionar e remover elementos de forma flexível.
* **Estruturas básicas da linguagem:** A lógica da aplicação utiliza laços de repetição tradicionais, como 'for-each' e 'while', para percorrer coleções, processar informações e realizar a leitura dos arquivos.


## Estrutura de Ficheiros (Diretório Raiz Único)

Como o projeto foi simplificado para fins de entrega e compilação direta, todos os ficheiros encontram-se na mesma pasta raiz:

1. 'Event.java': Classe modelo que representa um evento e contém as regras de validação.
2. 'EventCategory.java': Enumeração que padroniza os tipos de eventos permitidos.
3. 'EventManager.java': Controlador encarregue da lista na memória RAM e dos métodos de leitura/escrita no disco (I/O).
4. 'EventDialog.java': Interface gráfica do formulário de criação e edição.
5. 'EventPlannerGUI.java': Janela principal da aplicação que desenha o calendário, a agenda diária e contém o método 'main'.

## Como Compilar e Executar

### Pré-requisitos
- Java Development Kit (JDK) 8 ou superior instalado no sistema.
- Linha de comandos (Terminal) aberta na pasta do projeto.

### Passos
1. Certifique-se de que todos os ficheiros '.java' estão na mesma pasta (sem subpastas de pacotes).
2. Compile todas as classes correndo o comando:
   javac *.java
3. Invoque a Máquina Virtual Java chamando a classe de interface primária que hospeda a lógica do ponto de entrada do programa (método main ): 
   java EventPlannerGUI

### Autores
Jaqueline Paes de Almeida

João Vitor Salvini

Lucas Jacinto Mariano
