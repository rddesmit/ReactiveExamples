## Actor retry options
Het doel is om een bericht dat niet correct verwerkt is een N aantal keer opnieuw proberen te verwerken.


Als voorbeeld is een typische OAuth inlog flow nagebootst. Een url wordt ingewisseld voor een _code_ en deze wordt weer
ingewisseld voor een _token_. Beide operaties moeten 3 keer opnieuw geprobeerd worden als deze faalt. Daarbij mag de
laatste operatie niet langer dan 500ms duren.

```
() ->  url {
    code <- trade url for code
    token <- trade code for token within 500 ms
}
```


### Option 1: Mailbox
_akka_example_

```Bash
$ gradlew :akka_example:run
```

De mailbox is hier verantwoordelijk voor het N aantal keer opnieuw versturen van het mislukte bericht. Dit werkt omdat
de mailbox blijft bestaan zolang een supervisor een failure afhandeld.

De actor moet een bericht 'acknowledgement' versturen naar de mailbox als hij het bericht correct heeft verwerkt, pas
dan wordt het bericht niet meer verstuurd.

__Pros__
* Vereist bijna geen code
* Weinig overhead

__Cons__
* Logica moet zelf de 'acknowledgement' afhandelen

[Bron](http://doc.akka.io/docs/akka/current/contrib/peek-mailbox.html)


### Option 2: Retry actor
_akka_example_2_

```Bash
$ gradlew :akka_example2:run
```

De retry actor is hier verantwoordelijk voor het N aantal keer opnieuw versturen van het mislukte bericht. De retry actor
supervised de child icm het 'ask' pattern, en weet hierdoor of een bericht opnieuw verstuurd moet worden.

__Pros__
* Logica hoeft niets van de retry af te weten

__Cons__
* Meer overhead doordat ieder bericht door een extra actor heen moet
* Meer code

[Bron](https://gist.github.com/codetinkerhack/8206481)


### Option 3: Retry future
_akka_example_3_

```Bash
$ gradlew :akka_example3:run
```

De 'recover' mogelijheid van de future wordt gebruikt om de initiele actie opnieuw uit te voeren. Dit lijkt op de manier
waarop Rx zijn retry meganisme baseerd.

__Pros__
* Eenvoudig te gebruiken op hoog abstractie niveau
* Weinig code

__Cons__
* Veel overhead doordat gehele actie opnieuw wordt uitgevoerd

[Bron](https://blog.knoldus.com/2013/08/18/a-simple-way-to-implement-retry-support-for-akka-future-in-scala/)


# Keuze boom
Te maken keuzes
* Waar retry count bijhouden
* Waar berichten bijhouden en opnieuw versturen
* Waar herstart logica plaatsen
* Wel of geen state bewaren

---
1. __Geen__ state behouden
    1. __Zelfde__ retry logica voor iedere fout
        1. Retry verantwoordelijkheid bij actor met de logica
            1. PeekMailbox
                1. Count bijhouden
                2. Bericht opnieuw versturen
            2. (default) supervisor
                1. _Restart_ uitvoeren
        2. Retry verantwoordelijkheid _niet_ bij actor met de logica
            1. Retry Actor
                1. Count bijhouden
                2. Bericht opnieuw versturen
            2. (default) supervisor
                1. _Restart_ uitvoeren
    2. __Andere__ retry logica afhankelijk van state
        1. Retry count logica verantwoordelijkheid los van hoe de retry moet plaatsvinden
            1. Retry Actor
                1. Count bijhouden
                2. _Restart_ uitvoeren
            1. Post restart
                1. Bericht opnieuw versturen
2. __Wel__ state behouden
    1. _Resume_ ipv restart uitvoeren, daarna zie punt 1
    2. Zelf de count logica bijhouden