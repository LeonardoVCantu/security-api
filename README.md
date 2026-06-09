# API de Autenticação com Spring Boot e Java 21

Uma API REST de autenticação (Login e Registro) desenvolvida com foco em **Segurança**, resiliência de infraestrutura e boas práticas arquiteturais. O projeto implementa técnicas contra ataques automatizados, força bruta e enumeração de utilizadores.

---

## Tecnologias Utilizadas

* **Linguagem:** Java 21
* **Framework Core:** Spring Boot 4.0.x / Spring Framework 6.x
* **Segurança:** Spring Security & JWT (Json Web Token)
* **Persistência de Dados:** Spring Data JPA / Hibernate 7
* **Banco de Dados:** H2 Database (Banco em memória para testes)
* **Algoritmo de Hash:** BCrypt
* **Rate Limiting:** Bucket4j (Algoritmo Token Bucket)

---

## Mecanismos de Segurança Implementados

O grande diferencial desta API é a camada defensiva implementada para mitigar ataques comuns catalogados pela OWASP:

### 1. Proteção contra Força Bruta (Account Lockout)
* **Como funciona:** O sistema monitoriza as tentativas consecutivas de login falhadas por utilizador. Ao atingir o limite configurado (ex: 5 tentativas), a conta é temporariamente bloqueada.

### 2. Rate Limiting por IP (Mitigação de DoS e Credential Stuffing)
* **Como funciona:** Utiliza a biblioteca **Bucket4j**. Cada endereço IP possui um limite máximo de requisições por minuto gerido de forma thread-safe na memória através de um `ConcurrentHashMap`.
* **Proteção de Infraestrutura:** Se um script automatizado tentar inundar a API com requisições, ele é barrado imediatamente na camada de filtros do Servlet (`OncePerRequestFilter`), poupando o processamento pesado do hash criptográfico e consultas ao banco de dados, devolvendo o status **`HTTP 429 Too Many Requests`**.

### 3. Mitigação de Ataques de Tempo (Time-Based Side-Channel)
* **Como funciona:** Para evitar a *Enumeração de Usuários* (onde um atacante descobre se um e-mail existe no banco medindo milissegundos do tempo de resposta da API), o fluxo de autenticação foi blindado.
* **Lógica Defensiva:** Caso o e-mail não exista no banco de dados, o sistema executa um "processamento fantasma" calculando um hash BCrypt fictício. Isso garante que a API demore exatamente o mesmo tempo para responder, seja o utilizador válido ou inexistente.

### 4. Gestão Centralizada de Configurações (12-Factor App)
* Os limites de tentativas de login, tempo de bloqueio e capacidade do Rate Limit por IP foram totalmente externalizados no arquivo de propriedades (`application.yml`), permitindo ajustes em tempo de produção via variáveis de ambiente sem necessidade de novos deploys.

---
