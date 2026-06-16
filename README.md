# Java Event Planner

Este projeto é a implementação da versão compacta do **Java Event Planner**, desenvolvido como trabalho final da disciplina **SCC0504**. A aplicação fornece uma interface gráfica interativa (GUI) para agendamento e gestão de eventos, construída integralmente em Java.

## 📌 Funcionalidades Principais
- **Calendário Mensal Dinâmico:** Grelha interativa que destaca visualmente os dias com eventos agendados, mostrando a quantidade exata de eventos planeados através de marcadores HTML inteligentes.
- **Gestão de Eventos (CRUD):** Permite adicionar, editar e eliminar eventos através de formulários modais com validação rigorosa de dados. A adição de eventos herda automaticamente a data selecionada no calendário.
- **Categorias Codificadas por Cores:** Organização visual da agenda diária através de categorias (*Meeting, Birthday, Appointment, Other*).
- **Persistência de Dados Segura:** Os eventos são guardados e carregados automaticamente num ficheiro de texto local (`events_data.txt`), com tratamento avançado de exceções para evitar falhas (crashes) em caso de ficheiros corrompidos ou ausentes na primeira execução.
- **Sistema de Lembretes:** Notificações em ecrã (pop-up) no arranque da aplicação para alertar o utilizador sobre eventos nas próximas 24 horas.
- **Interface de Utilizador Premium (UX):** Integração com o *Look and Feel* nativo do sistema operativo, barra de estado para feedback em tempo real e atalhos rápidos de navegação (como o botão "Today").

## 🏗️ Arquitetura e Conceitos de OOP
O projeto foi desenhado respeitando princípios sólidos de Programação Orientada a Objetos para garantir um código limpo e de fácil manutenção:
- **Encapsulamento e Padrão MVC:** Isolamento total entre a lógica de persistência de dados (`EventManager`) e a interface gráfica (`EventPlannerGUI` e `EventDialog`). O modelo oculta os seus campos e expõe apenas os *getters/setters* necessários.
- **Tipagem Segura:** Utilização de *Enums* (`EventCategory`) para limitar e padronizar os tipos de eventos disponíveis no sistema.
- **Modern API de Datas:** Adoção do pacote `java.time` (`LocalDate`, `LocalTime`, `YearMonth`) de forma a garantir precisão e facilidade na manipulação do calendário e dos alarmes.

## 🚀 Como Compilar e Executar

### Pré-requisitos
- Java Development Kit (JDK) 8 ou superior instalado no sistema.

### Passos
1. Abra a linha de comandos (Terminal).
2. Navegue até ao diretório onde os ficheiros do projeto estão guardados.
3. Compile todas as classes Java de uma só vez com o comando: javac *.java
4. Execute a classe principal da aplicação para abrir a interface gráfica:java EventPlannerGUI

📂 Estrutura de Ficheiros
Event.java: O modelo de dados encapsulado que representa um evento isolado.

EventCategory.java: Enumeração com as categorias de eventos disponíveis.

EventManager.java: O Controlador responsável pela gestão em memória das listas de eventos e pelas operações de leitura/escrita no ficheiro (I/O).

EventDialog.java: Formulário modal (JDialog) para a criação e edição segura de eventos.

EventPlannerGUI.java: A janela principal (JFrame) que aloja o calendário de navegação e a lista da agenda diária, contendo o método main da aplicação.
