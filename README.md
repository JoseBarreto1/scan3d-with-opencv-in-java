# scan3d-with-opencv-in-java

Instructions for Linux (Ubuntu 16.04 or higher):

1. Using Eclipse Luna software for Java
2. Install Java version 8 or higher
3. Add the 2.4 version of the OpenCV library (.jar) to the library
4. Add the OpenCV library to the system (.iso)

#Instalando JAVA no Ubuntu 
1. Instalando o JRE/JDK padrão
> Se ainda não tiver, adicione o repositório do programa com o comando abaixo;

>>  	sudo add-apt-repository ppa:webupd8team/java

> Atualize o gerenciador de pacotes com o comando:

>> 	sudo apt-get update

> Agora use o comando abaixo para instalar o programa;

>>  	sudo apt-get install oracle-java9-installer

2. Gerenciando o Java (Opcional)
>Se você tiver mais de uma versão do Java instalado em seu sistema, execute o comando abaixo para definir o interpretador Java padrão:

>> 	sudo update-alternatives --config java

> Na tela que aparece, escolha e digite um número para selecionar uma versão do Java e depois tecle enter;
> Para definir o compilador Java padrão, executando o comando a seguir:

>> 	sudo update-alternatives --config javac

3. Definindo a variável de ambiente "JAVA_HOME"
> Para definir a variável de ambiente JAVA_HOME , que é necessária para alguns programas, primeiramente encontre o caminho da sua instalação Java:

>>	sudo update-alternatives --config java

> Ele retorna algo como:
Selection    Path                                            Priority   Status
------------------------------------------------------------
* 0            /usr/lib/jvm/java-7-oracle/jre/bin/java          1062      auto mode
  1            /usr/lib/jvm/java-6-openjdk-amd64/jre/bin/java   1061      manual mode
  2            /usr/lib/jvm/java-7-oracle/jre/bin/java          1062      manual mode

> O caminho da instalação para cada um é:
1. /usr/lib/jvm/java-7-oracle
2. /usr/lib/jvm/java-6-openjdk-amd64
3. /usr/lib/jvm/java-7-oracle
> Copie o caminho da sua instalação preferida e então edite o arquivo /etc/environment:

>>	sudo nano /etc/environment

> Nesse arquivo, adicione a seguinte linha (substituindo SEU_CAMINHO pelo caminho copiado):

>>	JAVA_HOME="SEU_CAMINHO"
JAVA_HOME="/usr/lib/jvm/java-8-oracle"
> Isto deve ser suficiente para definir a variável de ambiente. Agora recarregue este arquivo:

>>	source /etc/environment

>	Faça um teste executando:

>>	echo $JAVA_HOME

4. Install Apache Ant:
>> 	sudo apt-get install ant.

#######################################################################################
> Ter permissões de root e/ou admistrador

>> 	sudo su

> Se seu sistema é de 64 bits, use o comando abaixo. Se o link estiver desatualizado, acesse essa página, baixe a última versão e salve o arquivo com o nome eclipse.tar.gz;

>> 	wget -c http://eclipse.c3sl.ufpr.br/technology/epp/downloads/release/luna/SR2/eclipse-java-luna-SR2-linux-gtk-x86_64.tar.gz -O eclipse.tar.gz

> Depois de baixar, execute o comando abaixo para descomprimir o pacote baixado, para a pasta /opt/;

>> 	sudo tar -zxvf eclipse.tar.gz -C /opt/

> Renomeie a pasta criada. Se ao executar o comando abaixo ocorrer um erro com a mensagem iniciando com “mv: é impossível sobrescrever o não-diretório”, pule este passo;

>> 	sudo mv /opt/eclipse*/ /opt/eclipse

> Baixe e salve o ícone do programa na pasta criada;

>> 	sudo wget https://dl2.macupdate.com/images/icons128/11662.png -O /opt/eclipse/eclipse.png

> Se seu ambiente gráfico atual suportar, crie um lançador para o programa, executando o comando abaixo;

>> 	echo -e '[Desktop Entry]\n Version=1.0\n Name=eclipse\n Exec=/opt/eclipse/eclipse\n Icon=/opt/eclipse/eclipse.png\n Type=Application\n Categories=Application' | sudo tee /usr/share/applications/eclipse.desktop

> Já se a sua distribuição suportar, coloque o atalho na sua área de trabalho usando o gerenciador de arquivos do sistema ou o comando abaixo, e use-o para iniciar o programa.

>> 	sudo chmod +x /usr/share/applications/eclipse.desktop
>> 	cp /usr/share/applications/eclipse.desktop ~/Desktop

#######################################################################################
1. Obter a fonte
> O código-fonte está em um repositório Git público hospedado no Github. Clone e mude para a ramificação 2.4.
>> 	git clone https://github.com/Itseez/opencv.git
	cd opencv
	git checkout -b 2.4 origin/2.4
2. Compilar
> Primeiro, crie um diretório para a compilação no diretório clonado.

>>	mkdir build
	cd build
> Em seguida, configure o projeto com cmake com BUILD_SHARED_LIBS parâmetro desativado. A partir dos documentos : “Quando OpenCV é construído como um conjunto de bibliotecas estáticas (-DBUILD_SHARED_LIBS = OFF opção) a ligações biblioteca dinâmica Java é todo-suficiente, ou seja, não depende de outros libs OpenCV, mas inclui todo o código OpenCV dentro."

>>	cmake -D BUILD_SHARED_LIBS=OFF ..

> Certifique-se de que a saída contém algo assim na seção Java:
--   Java:
--     ant:                         /usr/bin/ant (ver 1.9.6)
--     JNI:                         /usr/lib/jvm/java-8-oracle/include /usr/lib/jvm/java-8-oracle/include/linux /usr/lib/jvm/java-8-oracle/include
--     Java tests:                  YES

> É importante notar que você pode continuar se as dependências Java não estiverem configuradas adequadamente, mas a versão compilada não incluirá ligações Java .

- Se o valor para ant for NO , então execute:
>> 	sudo apt-get install ant

- Se a variável de ambiente JAVA_HOME não está definido corretamente, ele faz com que NÃO aparecer para JNI. Neste caso, apenas exporte a variável para encaminhar o caminho correto, por exemplo com exportação JAVA_HOME = / usr / lib / jvm / java-8-oracle .

> Após as mudanças necessárias, execute novamente o comando e certifique-se de que tudo esteja corretamente configurado.

> Agora, comece a compilação:

>>	make -j8	//(A opção -j especifica o número de trabalhos a serem executados simultaneamente).

> Obs: Isso vai demorar um pouco, mas, eventualmente, cria um JAR (bin / opencv-xxx.jar) que contém as interfaces Java e os arquivos da biblioteca nativa.

3. Experimente
> Com as seguintes etapas, você pode usar a biblioteca compilada diretamente do Eclipse.
> Primeiro, registre a biblioteca nativa OpenCV:
- Em Window -> Preferences; vá para a seção: Java -> Build Path -> User Libraries .
- Clique em New para adicionar uma nova biblioteca e atribua-lhe um nome (por exemplo, OpenCV-2.4).
- Selecione-o e pressione: Add External JARs ... procure o seguinte arquivo JAR compilado: -> opencv / build / bin / opencv-xxx.jar.
- Especifique a localização da biblioteca (Native library location), selecionando a seguinte pasta: -> opencv / build / lib.

> Para o teste, crie um projeto simples:
> Crie um novo projeto Java.
> Adicione a biblioteca recém-criado (OpenCV-2.4) ao seu projeto java: clique no projeto -> properties -> Java Build Path -> Seção: Libraries -> Add Libraries -> User Library -> next -> marque: OpenCV-2.4 e dê Finish.


########################################################################################
