xquery version "1.0-ml";

(:~
: User: edelacruz
: Date: 4/24/18
: Time: 10:56 PM
: To change this template use File | Settings | File Templates.
:)

let $i := %s
let $uri := "/data/" || fn:string($i) || ".xml"
let $xml := element person {
    element id {$i},
    element content {
        "this is simply a test document with random data " || fn:string(xdmp:random(100))
    }
}
return xdmp:document-insert($uri, $xml, (), ("datum", "person", "entity"))

