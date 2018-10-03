# TokenCounter
Count tokens in program source file

## Usage

### Current folder, all files(include sub-folder's files)
    java jp.qpg.TokenCounter 
or

    java -jar TokenCounter.jar 

[output]

    > java jp.qpg.TokenCounter
    [target folder] C:\dev\TokenCounter\src
    [target extensions] (all)
    C:\dev\TokenCounter\src\main\java\jp\qpg\TokenCounter.java: 1417
    C:\dev\TokenCounter\src\test\java\test\TokenCounterTest.java: 138
    C:\dev\TokenCounter\src\test\resources\Example.java: 51
    3 files
    
### Specified folder, all files, token output
    java -DshowToken=true jp.qpg.TokenCounter C:\dev\TokenCounter\src\test\resources 
or

    java -DshowToken=true -jar TokenCounter.jar C:\dev\TokenCounter\src\test\resources 

[output]

    [target folder] C:\dev\TokenCounter\src\test\resources
    [target extensions] (all)
    ID: package
    ID: test
    MARK: ;
    COMMENT: /**
     * Example for test
     */
    ID: public
    ID: class
    ID: Example
    MARK: {
    COMMENT: /**
             * @param args not use
             */
    ID: public
    ID: static
    MARK: <
    ID: T
    MARK: >
    ID: void
    ID: main
    MARK: (
    ID: String
    MARK: [
    MARK: ]
    ID: args
    MARK: )
    MARK: {
    COMMENT: ///**/
    ID: int
    ID: あいうえお
    MARK: =
    NUMBER: 1
    MARK: ;
    ID: char
    ID: c
    MARK: =
    CHARACTER: '\''
    MARK: ;
    ID: System
    MARK: .
    ID: out
    MARK: .
    ID: println
    MARK: (
    STRING: "Hello\"\\\'!"
    MARK: .
    ID: length
    MARK: (
    MARK: )
    MARK: >=
    NUMBER: 1.25
    MARK: )
    MARK: ;
    MARK: }
    MARK: }
    C:\dev\TokenCounter\src\test\resources\Example.java: 51
    1 files

### Current folder, .java, .cs files, UTF-8 encoding
    java -Dfile.encoding=UTF-8 jp.qpg.TokenCounter . .java .cs
or

    java -Dfile.encoding=UTF-8 -jar TokenCounter.jar . .java .cs
