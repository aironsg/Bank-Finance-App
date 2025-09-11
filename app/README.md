# 💰 BankFinance - Aplicativo de Gestão Financeira

O **BankFinance** é um aplicativo mobile desenvolvido em **Kotlin + Android**, que simula operações financeiras em um banco digital.  
O app foi construído com foco em **aprendizado**, **boas práticas de arquitetura (Clean Architecture + MVVM)** e **uso de Firebase** como backend.

---

## 🚀 Funcionalidades Principais

- 📲 **Autenticação de Usuários**
    - Login, Registro e Recuperação de Conta.
    - Armazenamento seguro de dados no Firebase.

- 💵 **Depósitos**
    - Realize depósitos na sua conta e acompanhe os comprovantes.

- 📱 **Recargas**
    - Recarregue celulares com saldo ou cartão de crédito.
    - Recibo gerado automaticamente após cada operação.

- 🔄 **Transferências via PIX**
    - Envie e receba transferências instantâneas via chave PIX.
    - Fluxo com validação de senha de transação.
    - Geração automática de **recibo detalhado**.

- 🧾 **Extrato de Transações**
    - Visualize todas as movimentações financeiras (depósitos, recargas, transferências).
    - Filtros por tipo de operação.

- 💳 **Cartão de Crédito**
    - Consulta de limite disponível.
    - Uso como método de pagamento alternativo.

- 👤 **Perfil do Usuário**
    - Visualização e edição de informações básicas.

---

## 🏗️ Arquitetura e Tecnologias

- **Kotlin** como linguagem principal.
- **Clean Architecture + MVVM** para separação de camadas.
- **Firebase Realtime Database** para persistência em nuvem.
- **Hilt (Dependency Injection)** para injeção de dependência.
- **Coroutines + LiveData** para programação assíncrona e reativa.
- **Navigation Component** para navegação entre telas.
- **Material Design** para UI moderna e responsiva.

---

## 📸 Telas do Aplicativo
_

- Tela de Login  
  ![Login](docs/images/login.png)

- Tela de Home  
  ![Home](docs/images/home.png)

- Recibo PIX  
  ![Recibo PIX](docs/images/receipt_pix.png)

---

## 📦 Instalação

### 🔧 Pré-requisitos
- Android Studio (versão recomendada: **Giraffe ou superior**).
- SDK Android 24+.
- Conta Firebase configurada.

### ▶️ Rodando o projeto localmente

```bash
# Clone este repositório
git clone https://github.com/seuusuario/bankfinance.git

# Abra o projeto no Android Studio
# Configure seu arquivo google-services.json com sua conta Firebase

# Rode o app no emulador ou dispositivo físico
```


## 📐 Diagrama de Arquitetura

```mermaid
flowchart TD
    subgraph Presentation [Camada de Apresentação]
        View[Fragment/Activity] --> VM[ViewModel]
    end

    subgraph Domain [Camada de Domínio]
        VM --> UC[UseCases]
        UC --> Repo[Interface Repository]
    end

    subgraph Data [Camada de Dados]
        RepoImpl[RepositoryImpl] --> Firebase[(Firebase Realtime DB)]
        RepoImpl --> LocalDB[(Room / SharedPrefs - Futuro)]
    end

    View -->|UI Events| VM
    VM -->|LiveData/StateView| View
```
📥 Download do APK

A versão mais recente pode ser baixada aqui:
👉 Download APK

🧪 Testes

Testes unitários implementados para fluxos principais (ex.: Transferências PIX).

Camada de repositório e useCases isolados para testes independentes.

📖 Próximos Passos

 Implementar notificações push para novas transferências.

 Melhorar relatórios financeiros (gráficos de receitas/despesas).

 Publicar na Play Store.

👨‍💻 Autores

Desenvolvido por [Airon silva](https://www.linkedin.com/in/airon-silva-dev/).
📧 Contato: airon_gm@hotmail.com

🔗 LinkedIn: https://www.linkedin.com/in/airon-silva-dev/

📜 Licença

Este projeto é distribuído sob a licença MIT. Veja o arquivo LICENSE
 para mais detalhes.


---

