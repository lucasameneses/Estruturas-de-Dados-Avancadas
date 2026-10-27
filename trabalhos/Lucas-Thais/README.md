# Estruturas de Dados Avançadas - Trabalho de Persistência Parcial

## Equipe

Lucas (matrícula XXXXXX) e Thais (matrícula XXXXXX)

## Descrição

Árvore binária de busca (não balanceada) com persistência parcial, implementada pelo método de cópia de nós (_node copying_) visto em sala.
Toda inclusão ou remoção cria uma nova versão; consultas (`SUC` e `IMP`) podem ser feitas em qualquer versão.

- **Linguagem de programação utilizada:** Java 25 (OpenJDK 25)
- **Compilação:** `make build` (gera as classes em `bin/`)
- **Execução:** `make run INPUT=arquivo.in`

### Funcionamento

- Cada nó guarda os valores originais dos campos (`key`, `left`, `right`) e um vetor de até `MAX_MODS = 2` modificações. O tamanho segue a regra `2p` do método, já que cada nó de uma árvore binária tem grau de entrada `p = 1`.
- Para ler um campo na versão `v`, parte-se do valor original e aplicam-se as modificações com versão menor ou igual a `v`.
- Quando um campo precisa mudar na versão atual:
  1. Se o nó foi criado na versão atual, o campo original é alterado diretamente.
  2. Se o nó já tem uma modificação desse campo na versão atual, ela é sobrescrita (uma mesma operação pode alterar vários campos).
  3. Se ainda há espaço no vetor de modificações, uma nova modificação é adicionada.
  4. Caso contrário, o nó é copiado com seus valores mais recentes e com o vetor de modificações vazio, e o pai passa a apontar para a cópia. Essa alteração no pai segue as mesmas regras e pode gerar novas cópias em cascata até a raiz.
- Cada nó guarda um ponteiro para o pai (`liveParent`) válido apenas na versão mais recente. Ele não é versionado, porque apenas a versão mais recente é alterada (persistência parcial).
- As raízes de cada versão ficam em um vetor de 1001 posições, pois há no máximo 1000 inclusões e remoções.

### Complexidade

- Cada inclusão ou remoção altera `O(1)` campos além da busca na árvore.
- Seja o potencial `Φ` proporcional ao número de modificações guardadas em nós da versão mais recente. Uma cópia libera as `MAX_MODS` modificações do nó copiado e gera no máximo uma modificação no pai, então o custo das cópias em cascata é pago pela queda de `Φ`.
- Assim, cada operação tem custo adicional multiplicativo `O(1)` amortizado, e o espaço total para `v` versões é `O(v)`.
- Consultas na versão `v` leem cada campo em `O(MAX_MODS) = O(1)`, mantendo a complexidade de uma árvore binária de busca comum (`O(h)` para sucessor e `O(n)` para impressão).

## Estruturas e funções

### `Field`

- Definida em `src/Field.java`
- Enumeração dos campos versionados de um nó: `KEY` (chave), `LEFT` (filho esquerdo) e `RIGHT` (filho direito).

### `Mod`

- Definida em `src/Mod.java`
- Representa uma modificação de campo guardada dentro de um nó.
- O campo `version` é a versão a partir da qual a modificação vale.
- O campo `field` indica qual campo do nó foi modificado.
- O campo `key` guarda o novo valor quando `field` é `KEY`.
- O campo `child` guarda o novo filho quando `field` é `LEFT` ou `RIGHT`.
- O construtor `Mod(version, field, key, child)` cria a modificação.

### `Node`

- Definida em `src/Node.java`
- Nó da árvore binária de busca persistente.
- A constante `MAX_MODS` é o número máximo de modificações por nó (2).
- A constante `LATEST` é usada como versão para ler o valor mais recente de um campo.
- Os campos `key`, `left` e `right` guardam os valores originais da chave e dos filhos.
- O campo `liveParent` aponta para o pai na versão mais recente.
- O campo `createdVersion` é a versão em que o nó foi criado.
- O campo `mods[]` guarda as modificações em ordem crescente de versão, e `modCount` indica quantas estão em uso.
- O construtor `Node(key, left, right, createdVersion)` cria o nó com os valores originais.
- A função `key(version)` retorna a chave do nó na versão `version`.
- A função `child(side, version)` retorna o filho do lado `side` (`LEFT` ou `RIGHT`) na versão `version`.
- A função `latestChild(side)` retorna o filho do lado `side` na versão mais recente.
- A função `setOriginal(field, newKey, newChild)` altera diretamente um campo original.
- A função `findMod(version, field)` retorna a modificação do campo `field` feita na versão `version`, ou `null` se não existir.
- A função `hasFreeMod()` indica se ainda há espaço no vetor de modificações.
- A função `addMod(mod)` adiciona uma modificação ao vetor.
- A função `copyLatest(version)` cria um novo nó na versão `version` com os valores mais recentes deste nó e sem modificações.

### `PersistentBST`

- Definida em `src/PersistentBST.java`
- Árvore binária de busca com persistência parcial.
- A constante `MAX_VERSIONS` é o número máximo de versões (1001).
- A constante `NO_SUCCESSOR` é o texto impresso quando não há sucessor (`inf`).
- O campo `roots[]` guarda a raiz de cada versão.
- O campo `currentVersion` é a versão mais recente.
- A função `insert(key)` cria uma nova versão e insere `key` como folha. Chaves repetidas são inseridas à direita.
- A função `remove(key)` cria uma nova versão e remove uma ocorrência de `key`, se existir. Um nó com dois filhos recebe a chave do seu sucessor, e o sucessor é removido.
- A função `successor(key, version)` retorna a menor chave estritamente maior que `key` na versão `version`, ou `inf`.
- A função `print(version)` retorna as chaves da versão `version` em ordem crescente, separadas por vírgula e espaço. O percurso em ordem é iterativo, com pilha, porque a árvore pode ficar degenerada.
- A função `startNewVersion()` cria uma nova versão que começa com a mesma raiz da anterior.
- A função `existingVersion(version)` retorna `version` se ela existir, ou a versão mais recente caso contrário.
- A função `find(key)` retorna um nó da versão mais recente com a chave `key`, ou `null`.
- A função `minimum(node)` retorna o nó de menor chave da subárvore de `node` na versão mais recente.
- A função `unlink(node)` remove um nó com no máximo um filho, ligando esse filho ao pai do nó (ou à raiz).
- A função `sideOf(parent, child)` retorna de qual lado de `parent` está o filho `child`.
- A função `write(node, field, key, child)` altera um campo de `node` na versão atual seguindo as regras descritas em [Funcionamento](#funcionamento) e retorna o nó que fica na versão mais recente (o próprio nó ou sua cópia).
- A função `copy(node, field, key, child)` copia `node` já com a alteração pedida, atualiza o pai dos filhos e faz o pai (ou a raiz da versão) apontar para a cópia.
- A função `adoptChildren(node)` faz os filhos de `node` apontarem para ele como pai.

### `Main`

- Definida em `src/Main.java`
- Programa principal.
- A função `main(args)` lê o arquivo de entrada, executa cada linha e imprime toda a saída no final.
- A função `execute(command, tree, out)` executa um comando (`INC`, `REM`, `SUC` ou `IMP`) na árvore.
- A função `echo(out, command, result)` adiciona à saída a linha do comando seguida do seu resultado.
