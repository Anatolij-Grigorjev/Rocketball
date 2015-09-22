/**
 * Created by anatolij on 22/09/15.
 */

def rnd = new Random()
def bytes = new byte[15]
rnd.nextBytes(bytes)

def l = bytes.toList()


println "Natural list order: $l"
println "Sorted list order: ${l.sort { a, b -> a <=> b }}"
println "reverse sorted list order: ${l.sort { a, b -> b <=> a }}"

