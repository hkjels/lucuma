(ns lucuma-test
  (:require [cemerick.cljs.test :as t :refer-macros [deftest is use-fixtures]]
            [lucuma :as l :refer-macros [defwebcomponent]])
  (:refer-clojure :exclude [methods]))

(def ^:private tests-node "tests-appends")

(defn append-tests-node
  []
  (let [el (.createElement js/document "div")]
    (set! (.-id el) tests-node)
    (.appendChild js/document.body el)))

(defn delete-tests-node
  []
  (let [el (.getElementById js/document tests-node)]
    (.remove el)))

(defn append
  ([t] (append t nil nil))
  ([t is id]
   (let [id (or id t)
         el (if is (.createElement js/document t is) (.createElement js/document t))]
    (set! (.-id el) id)
    (.appendChild (.getElementById js/document tests-node) el))))

(defn- by-id [id] (.getElementById js/document id))

(deftest ignored-keys
  (is (empty? (l/ignored-keys {:document ""})))
  (is (empty? (l/ignored-keys {:document "" :style ""})))
  (is (= #{:documemt} (l/ignored-keys {:documemt "" :style ""}))))

(deftest validate-definition
  (is (thrown? js/Error (l/register "")))
  (is (thrown? js/Error (l/register {:unknown-key nil}))))

(defwebcomponent test-sr-1)
(defwebcomponent test-sr-2
  :document "hello")
(defwebcomponent test-sr-3
  :style "* {background: red;}")

(deftest create-shadow-root-when-needed
  (is (nil? (.-shadowRoot (by-id "test-sr-1"))))
  (is (not (nil? (.-shadowRoot (by-id "test-sr-2")))))
  (is (not (nil? (.-shadowRoot (by-id "test-sr-3"))))))

(deftest shadow-root
  (is (nil? (l/shadow-root (.createElement js/document "div"))))
  (is (nil? (l/shadow-root (.createElement js/document "test-sr-1"))))
  (is (not (nil? (l/shadow-root (.createElement js/document "test-sr-2")))))
  (is (not (nil? (l/shadow-root (.createElement js/document "test-sr-2") :test-sr-2))))
  (is (nil? (l/shadow-root (.createElement js/document "test-sr-2") :wrong-element-name))))

(defwebcomponent test-prototype-1)
(defwebcomponent test-prototype-2
  :host :button)
(defwebcomponent test-prototype-3
  :host :test-prototype-2)

(defwebcomponent test-prototype-definition-1
  :host :test-prototype-3)
(defwebcomponent test-prototype-definition-2
  :host :test-prototype-polymer
  :extends :button)
(defwebcomponent test-prototype-definition-3
  test-prototype-2)
(defwebcomponent test-prototype-definition-4
  [arg]
  :document (inc arg))
(defwebcomponent test-prototype-definition-5
  [arg]
  test-prototype-2
  :document (inc arg))
(defwebcomponent test-prototype-definition-6
  :host :test-prototype-1)
(defwebcomponent test-prototype-definition-7
  :properties {:property1 "default" :property2 "default"})
(defwebcomponent test-prototype-definition-8
  test-prototype-definition-7
  :properties {:property2 "another-default"})

(defwebcomponent test-prototype-definition-fail-1
  :host :test-prototype-polymer)

(deftest webcomponent-reuse
  (is (= :button (:host test-prototype-definition-3)))
  (is (= "default" (get-in test-prototype-definition-8 [:properties :property1])))
  (is (= "another-default" (get-in test-prototype-definition-8 [:properties :property2])))
  (is (= :button (:host (test-prototype-definition-5 0)))))

(deftest webcomponent-as-fn
  (is (= 2 (:document (test-prototype-definition-4 1)))))

(deftest parse-host-type
  (is (nil? (l/host-type nil)))
  (is (= :div (l/host-type :div)))
  (is (nil? (l/host-type {:att ""})))
  (is (= :div (l/host-type [:div {:att ""}]))))

(deftest parse-host-attributes
  (is (nil? (l/host-attributes nil)))
  (is (nil? (l/host-attributes :div)))
  (is (= {:att ""} (l/host-attributes {:att ""})))
  (is (= {:att ""} (l/host-attributes [:div {:att ""}]))))

(defwebcomponent test-host-attributes
  :host {:a "A"})

(deftest host-attributes
  (is (= "A" (.getAttribute (by-id "test-host-attributes") "a"))))

(defwebcomponent test-extends-1)
(defwebcomponent test-extends-2
  :host :div)
(defwebcomponent test-extends-3
  :host :test-extends-1)
(defwebcomponent test-extends-4
  :host :test-extends-2)
(defwebcomponent test-extends-5
  :host :non-lucu-element
  :extends :div)
(defwebcomponent test-extends-6
  :host :test-extends-4)
(defwebcomponent test-extends-7
  :host [:test-extends-4 {}])
(defwebcomponent test-extends-8
  :host :span
  :extends :div)
(defwebcomponent test-extends-9
  :extends :div)
(defwebcomponent test-extends-fail-1
  :host :non-lucu-element)

(deftest host-type->extends
  (is (nil? (l/host-type->extends nil)))
  (is (= :div (l/host-type->extends :div)))
  (is (nil? (l/host-type->extends :test-extends-1)))
  (is (= :div (l/host-type->extends :test-extends-2)))
  (is (nil? (l/host-type->extends :test-extends-3)))
  (is (= :div (l/host-type->extends :test-extends-4)))
  (is (= :div (l/host-type->extends :test-extends-5)))
  (is (= :div (l/host-type->extends :test-extends-6)))
  (is (= :div (l/host-type->extends :test-extends-7)))
  (is (= :div (l/host-type->extends :test-extends-8)))
  (is (nil? (l/host-type->extends :test-extends-9)))
  (is (thrown? js/Error (l/host-type->extends :test-extends-fail-1))))

(deftest extends-right-prototype
  (is (instance? js/HTMLUnknownElement (.createElement js/document "unknown")))
  (is (instance? js/HTMLElement (.createElement js/document "test-unknown")))
  ;; TODO Fails with polymer's polyfill: https://github.com/Polymer/CustomElements/issues/100
  (is (not (instance? js/HTMLUnknownElement (.createElement js/document "test-unknown"))))
  (is (instance? js/HTMLElement (.createElement js/document "test-prototype-1")))
  (is (instance? js/HTMLButtonElement (.createElement js/document "button" "test-prototype-2")))
  (is (instance? js/HTMLButtonElement (.createElement js/document "button" "test-prototype-3"))))

(deftest element-name
  (is (= :button (l/element-name (.createElement js/document "button"))))
  (is (= :test-prototype-1 (l/element-name (.createElement js/document "test-prototype-1"))))
  (is (= :test-prototype-2 (l/element-name (.createElement js/document "button" "test-prototype-2")))))

(defwebcomponent test-register)

(deftest register-is-idempotent
  (is (true? (l/register test-register)) "first registration")
  (is (not (true? (l/register test-register))) "second registration")
  (is (thrown? js/Error (l/register nil)))
  (is (thrown? js/Error (l/register (fn [] nil)))))

(deftest is-lucuma-element
  (is (not (l/lucuma-element? nil)))
  (is (not (l/lucuma-element? (.createElement js/document "div"))))
  (is (l/lucuma-element? (.createElement js/document "test-prototype-1")))
  ;; TODO  fails with polymer's polyfill: https://github.com/Polymer/CustomElements/issues/101
  (is (not (l/lucuma-element? (.createElement js/document "test-prototype-2"))))
  (is (l/lucuma-element? (.createElement js/document "button" "test-prototype-2")))
  (is (not (l/lucuma-element? (.createElement js/document "test-prototype-3"))))
  (is (l/lucuma-element? (.createElement js/document "button" "test-prototype-3"))))

(deftest element-name
  (is (= nil (l/element-name nil)))
  (is (= :div (l/element-name (.createElement js/document "div"))))
  (is (= :test-prototype-1 (l/element-name (.createElement js/document "test-prototype-1"))))
  (is (= :test-prototype-2 (l/element-name (.createElement js/document "button" "test-prototype-2")))))

(deftest property-definition
  (is (nil? (l/validate-property-definition! "name" "default")))
  (is (nil? (l/validate-property-definition! "name" nil)))
  (is (thrown? js/Error (l/validate-property-definition! "name" {})))
  (is (thrown? js/Error (l/validate-property-definition! "name" {:type js/String})))
  (is (nil? (l/validate-property-definition! "name" {:default "default"})))
  (is (nil? (l/validate-property-definition! "name" {:default nil :type js/String})))
  (is (nil? (l/validate-property-definition! "name" {:default nil :type js/Object})))
  (is (nil? (l/validate-property-definition! "name" {:default (atom true) :type js/Object})))
  (is (nil? (l/validate-property-definition! "name" {:default :label :type js/Object})))
  (is (nil? (l/validate-property-definition! "name" {:default "default" :type js/String})))
  (is (thrown? js/Error (l/validate-property-definition! "name" {:default "default" :type js/Boolean}))))

(deftest get-property-definition-default
  (is (= "" (l/get-property-definition-default {:default ""})))
  (is (= "" (l/get-property-definition-default ""))))

(deftest get-property-definition-type
  (is (= js/String (l/get-property-definition-type {:default ""})))
  (is (= js/String (l/get-property-definition-type {:type js/String :default nil})))
  (is (= js/Boolean (l/get-property-definition-type {:default false})))
  (is (= js/Object (l/get-property-definition-type {:default {}})))
  (is (= js/Object (l/get-property-definition-type {:default nil}))))

(deftest property-definition-attributes
  (is (= true (l/property-definition-attributes? {:attributes? true})))
  (is (= false (l/property-definition-attributes? {:attributes? false})))
  (is (= false (l/property-definition-attributes? {:type js/Function})))
  (is (= true (l/property-definition-attributes? {:type js/Boolean})))
  (is (= false (l/property-definition-attributes? {:attributes? false :type js/Boolean}))))

(deftest property-definition-events
  (is (= true (l/property-definition-events? {:events? true})))
  (is (= false (l/property-definition-events? {:events? false})))
  (is (= false (l/property-definition-events? {:type js/Function})))
  (is (= true (l/property-definition-events? {:type js/Boolean})))
  (is (= false (l/property-definition-events? {:events? false :type js/Boolean}))))

(defwebcomponent test-property-1
  :properties {:property nil})

(defwebcomponent test-property-fail-1
  :properties {:invalid-property-name nil})
(defwebcomponent test-property-fail-2
  :properties {:id nil})
(defwebcomponent test-property-fail-3
  :host :img
  :properties {:src nil})

(deftest property-name
  (is (l/register test-property-1))
  (is (thrown? js/Error (l/register test-property-fail-1)))
  (is (thrown? js/Error (l/register test-property-fail-2)))
  (is (thrown? js/Error (l/register test-property-fail-3))))

(deftest properties
  (is (nil? (.-property (.createElement js/document "test-property-1")))))

(defwebcomponent test-method-1
  :methods {:method1 (fn [] 1)
            :method2 (fn [] {:key "value"})
            :method3 (fn [_ o] (get o "key"))
            :method4 (fn [& args] args)})

(defwebcomponent test-method-2
  :methods {:invalid-method-name nil})

(defwebcomponent test-method-3
  :methods {:constructor nil})

(deftest methods
  (is (= 1 (.method1 (.createElement js/document "test-method-1"))))
  (is (= "value" (aget (.method2 (.createElement js/document "test-method-1")) "key")))
  (is (= "value" (.method3 (.createElement js/document "test-method-1") #js {:key "value"})))
  (is (not (nil? (first (.method4 (.createElement js/document "test-method-1"))))))
  (is (nil? (second (.method4 (.createElement js/document "test-method-1")))))
  (is (thrown? js/Error (l/register test-method-2)))
  (is (thrown? js/Error (l/register test-method-3))))

(defn wrap-registration
  [f]
  (append-tests-node)

  (l/register test-sr-1)
  (l/register test-sr-2)
  (l/register test-sr-3)
  (append "test-sr-1")
  (append "test-sr-2")
  (append "test-sr-3")

  (l/register test-prototype-1)
  (l/register test-prototype-2)
  (l/register test-prototype-3)

  (l/register test-extends-1)
  (l/register test-extends-2)
  (l/register test-extends-3)
  (l/register test-extends-4)
  (l/register test-extends-5)
  (l/register test-extends-6)
  (l/register test-extends-7)
  (l/register test-extends-8)
  (l/register test-extends-9)

  (l/register test-method-1)

  (l/register test-host-attributes)
  (append "test-host-attributes")

  (f)

  (delete-tests-node))

(use-fixtures :once wrap-registration)
