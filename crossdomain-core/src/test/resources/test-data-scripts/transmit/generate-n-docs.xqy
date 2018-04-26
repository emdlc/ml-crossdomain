xquery version "1.0-ml";

(:~
: User: edelacruz
: Date: 4/24/18
: Time: 10:56 PM
: To change this template use File | Settings | File Templates.
:)

for $i in 1 to %s
let $uri := "/data/" || xdmp:hash64(fn:string($i)) || ".xml"
let $xml := element envelope {
    element id {$i},
    element content {
        "this is simply a test document with random data " || fn:string(xdmp:random(100))
    }
}
return xdmp:document-insert($uri, $xml, (), "datum")

