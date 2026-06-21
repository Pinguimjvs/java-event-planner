# Java Event Planner

Este projeto é a implementação da versão compacta de um sistema de agendamento e gestão de eventos com interface gráfica (GUI) para a disciplina SCC0504 - Programação Orientada a Objetos.

## Funcionalidades Principais

- **Calendário Mensal Interativo:** Uma grelha que calcula dinamicamente os dias do mês e destaca visualmente as datas que possuem compromissos através de um marcador de texto puro '(*)' e alteração da cor de fundo.
- **Gestão Completa de Eventos (CRUD):** Criação, leitura, edição e eliminação de eventos através de janelas modais ('JDialog') com validação de campos (o título não pode ser vazio e as datas/horas devem ser válidas).
- **Categorização Estruturada:** Organização de compromissos por categorias fixas ('Meeting', 'Birthday', 'Appointment', 'Other') utilizando Enumerações ('Enum').
- **Persistência de Dados Local (I/O):** Gravação e carregamento automático dos eventos num ficheiro de texto local ('events_data.txt'). O sistema possui tolerância a falhas, ignorando linhas corrompidas e tratando a ausência do ficheiro na primeira execução sem deitar a aplicação abaixo.
- **Lembretes Automáticos:** Varredura imediata na inicialização do sistema para exibir um alerta visual ('JOptionPane') caso existam eventos com alarmes programados para o dia corrente.

## Conceitos de Orientação a Objetos Aplicados

- **Encapsulamento:** Os atributos da classe 'Event' são estritamente privados ('private') e o acesso ou modificação dos dados é controlado de forma segura através de métodos *getters* e *setters* com validações embutidas.
- **Reutilização de Construtores:** Uso do comando 'this(...)' para sobrecarga de construtores, evitando a repetição de código e centralizando a inicialização do objeto.
- **Estruturas de Dados Dinâmicas:** Utilização de 'List' e 'ArrayList' para gerir as coleções de eventos em memória RAM de forma flexível.
- **Lógica de Programação Tradicional:** Todo o processamento, filtragem de datas e leitura de ficheiros utiliza laços de repetição tradicionais ('for-each' e 'while'), adequados para o nível de introdução à programação em Java.

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
