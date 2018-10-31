package hjelpeklasser;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class BinTre<T> implements Iterable         // et generisk binærtre
{
    private static final class Node<T>  // en indre nodeklasse
    {
        private T verdi;            // nodens verdi
        private Node<T> venstre;    // referanse til venstre barn/subtre
        private Node<T> høyre;      // referanse til høyre barn/subtre

        private Node(T verdi, Node<T> v, Node<T> h)    // konstruktør
        {
            this.verdi = verdi; venstre = v; høyre = h;
        }

        private Node(T verdi) { this.verdi = verdi; }  // konstruktør

    } // class Node<T>

    private Node<T> rot;      // referanse til rotnoden
    private int antall;       // antall noder i treet
    public int endringer = 0;

    public BinTre() { rot = null; antall = 0; }          // konstruktør

    public BinTre(int[] posisjon, T[] verdi)  // konstruktør
    {
        if (posisjon.length > verdi.length) throw new
                IllegalArgumentException("Verditabellen har for få elementer!");

        for (int i = 0; i < posisjon.length; i++) leggInn(posisjon[i],verdi[i]);
    }

    public final void leggInn(int posisjon, T verdi) {
        if (posisjon < 1) throw new
                IllegalArgumentException("Posisjon (" + posisjon + ") < 1!");

        Node<T> p = rot, q = null;    // nodereferanser

        int filter = Integer.highestOneBit(posisjon) >> 1;   // filter = 100...00

        while (p != null && filter > 0)
        {
            q = p;
            p = (posisjon & filter) == 0 ? p.venstre : p.høyre;
            filter >>= 1;  // bitforskyver filter
        }

        if (filter > 0) throw new
                IllegalArgumentException("Posisjon (" + posisjon + ") mangler forelder!");
        else if (p != null) throw new
                IllegalArgumentException("Posisjon (" + posisjon + ") finnes fra før!");

        p = new Node<>(verdi);          // ny node

        if (q == null) rot = p;         // tomt tre - ny rot
        else if ((posisjon & 1) == 0)   // sjekker siste siffer i posisjon
            q.venstre = p;                // venstre barn til q
        else
            q.høyre = p;                  // høyre barn til q
        endringer++;
        antall++;                       // en ny verdi i treet
    }

    public int antall() { return antall; }               // returnerer antallet

    public boolean tom() { return antall == 0; }         // tomt tre?

    private Node<T> finnNode(int posisjon)  // finner noden med gitt posisjon
    {
        if (posisjon < 1) return null;

        Node<T> p = rot;   // nodereferanse
        int filter = Integer.highestOneBit(posisjon >> 1);   // filter = 100...00

        for (; p != null && filter > 0; filter >>= 1)
            p = (posisjon & filter) == 0 ? p.venstre : p.høyre;

        return p;   // p blir null hvis posisjon ikke er i treet
    }

    public boolean finnes(int posisjon)
    {
        return finnNode(posisjon) != null;
    }

    public T hent(int posisjon)
    {
        Node<T> p = finnNode(posisjon);

        if (p == null) throw new
                IllegalArgumentException("Posisjon (" + posisjon + ") finnes ikke i treet!");

        return p.verdi;
    }

    public T oppdater(int posisjon, T nyverdi)
    {
        Node<T> p = finnNode(posisjon);

        if (p == null) throw new
                IllegalArgumentException("Posisjon (" + posisjon + ") finnes ikke i treet!");

        T gammelverdi = p.verdi;
        p.verdi = nyverdi;

        endringer++;
        return gammelverdi;
    }

    public T fjern(int posisjon) {
        Node<T> p = finnNode(posisjon);

        if(p.høyre != null && p.venstre != null) {
            throw new IllegalArgumentException("Noden er ikke en bladnode");
        }

        T verdi = p.verdi;
        Node<T> q;
        if (posisjon % 2 == 0) {
            q = finnNode(posisjon / 2);
            q.venstre = null;
        } else {
            q = finnNode((posisjon - 1) / 2);
            q.høyre = null;
        }
        endringer++;
        return verdi;
    }

    public void nivåorden(Oppgave<? super T> oppgave)    // ny versjon
    {
        if (tom()) return;                   // tomt tre
        Kø<Node<T>> kø = new TabellKø<>();   // Se Avsnitt 4.2.3
        kø.leggInn(rot);                     // legger inn roten

        while (!kø.tom())                    // så lenge køen ikke er tom
        {
            Node<T> p = kø.taUt();             // tar ut fra køen
            oppgave.utførOppgave(p.verdi);     // den generiske oppgaven

            if (p.venstre != null) kø.leggInn(p.venstre);
            if (p.høyre != null) kø.leggInn(p.høyre);
        }
    }

    public int[] nivåer()   // returnerer en tabell som inneholder nivåantallene
    {
        if (tom()) return new int[0];       // en tom tabell for et tomt tre

        int[] a = new int[8];               // hjelpetabell
        Kø<Node<T>> kø = new TabellKø<>();  // hjelpekø
        int nivå = 0;                       // hjelpevariabel

        kø.leggInn(rot);    // legger roten i køen

        while (!kø.tom())   // så lenge som køen ikke er tom
        {
            // utvider a hvis det er nødvendig
            if (nivå == a.length) a = Arrays.copyOf(a,2*nivå);

            int k = a[nivå] = kø.antall();  // antallet på dette nivået

            for (int i = 0; i < k; i++)  // alle på nivået
            {
                Node<T> p = kø.taUt();

                if (p.venstre != null) kø.leggInn(p.venstre);
                if (p.høyre != null) kø.leggInn(p.høyre);
            }

            nivå++;  // fortsetter på neste nivå
        }

        return Arrays.copyOf(a, nivå);  // fjerner det overflødige
    }

    private static <T> void preorden(Node<T> p, Oppgave<? super T> oppgave)
    {
        while (true)
        {
            oppgave.utførOppgave(p.verdi);
            if (p.venstre != null) preorden(p.venstre,oppgave);
            if (p.høyre == null) return;      // metodekallet er ferdig
            p = p.høyre;
        }
    }

    public void preorden(Oppgave<? super T> oppgave)
    {
        if (!tom()) preorden(rot,oppgave);  // sjekker om treet er tomt
    }

    public void inorden(Oppgave<? super T> oppgave) {
        if(!tom()) {
            inorden(rot, oppgave);
        }
    }

    private void inorden(Node<T> p, Oppgave<? super T> oppgave) {
        while (true) {
            if (p.venstre != null) inorden(p.venstre, oppgave);  // til venstre barn
            oppgave.utførOppgave(p.verdi);                       // utfører oppgaven
            if(p.høyre == null) return;      // til høyre barn
            p = p.høyre;
        }
    }

    public void postOrden(Oppgave<? super T> oppgave) {
        if(!tom()) {
            postOrden(rot, oppgave);
        }
    }

    private void postOrden(Node<T> p, Oppgave<? super T> oppgave) {
        if (p.venstre != null) postOrden(p.venstre,oppgave);  // til venstre barn
        if (p.høyre != null) postOrden(p.høyre,oppgave);      // til høyre barn
        oppgave.utførOppgave(p.verdi);                       // utfører oppgaven
    }

    public void nullstill() {
        if(!tom()) {
            nullstill(rot);
        }
        rot = null;
        antall = 0;
    }

    private void nullstill(Node<T> p) {
        if (p.venstre != null) nullstill(p.venstre);  // til venstre barn
        if (p.høyre != null) nullstill(p.høyre);      // til høyre barn
        p.venstre = null;
        p.høyre = null;
        p.verdi = null;
    }

    private class InordenIterator implements Iterator<T>
    {
        private Stakk<Node<T>> s = new TabellStakk<>();
        private Node<T> p = null;
        private int iteratorEndringer = endringer;

        private Node<T> først(Node<T> q)   // en hjelpemetode
        {
            while (q.venstre != null)        // starter i q
            {
                s.leggInn(q);                  // legger q på stakken
                q = q.venstre;                 // går videre mot venstre
            }
            return q;                        // q er lengst ned til venstre
        }

        private InordenIterator()          // konstruktør
        {
            if (tom()) return;               // treet er tomt
            p = først(rot);                  // bruker hjelpemetoden
        }

        @Override
        public T next()
        {
            if (!hasNext()) throw new NoSuchElementException("Ingen verdier!");
            if (iteratorEndringer != endringer) {
                throw new IllegalArgumentException("Det er gjort endringer siden iteratoren startet");
            }

            T verdi = p.verdi;                        // tar vare på verdien

            if (p.høyre != null) p = først(p.høyre);  // p har høyre subtre
            else if (s.tom()) p = null;               // stakken er tom
            else p = s.taUt();                        // tar fra stakken

            return verdi;                             // returnerer verdien
        }

        @Override
        public boolean hasNext()
        {
            return p != null;
        }

    }
    public Iterator<T> iterator()     // skal ligge i class BinTre
    {
        return new InordenIterator();
    }

    public Iterator<T> omvendtIterator()     // skal ligge i class BinTre
    {
        return new OmvendtInordenIterator();
    }


    //Omvendt iterator
    private class OmvendtInordenIterator implements Iterator<T>
    {
        private Stakk<Node<T>> s = new TabellStakk<>();
        private Node<T> p = null;
        private int iteratorEndringer = endringer;

        private Node<T> først(Node<T> q)   // en hjelpemetode
        {
            while (q.høyre != null)        // starter i q
            {
                s.leggInn(q);                  // legger q på stakken
                q = q.høyre;                 // går videre mot venstre
            }
            return q;                        // q er lengst ned til venstre
        }

        private OmvendtInordenIterator()          // konstruktør
        {
            if (tom()) return;               // treet er tomt
            p = først(rot);                  // bruker hjelpemetoden
        }

        @Override
        public T next()
        {
            if (!hasNext()) throw new NoSuchElementException("Ingen verdier!");
            if (iteratorEndringer != endringer) {
                throw new IllegalArgumentException("Det er gjort endringer siden iteratoren startet");
            }

            T verdi = p.verdi;                        // tar vare på verdien

            if (p.venstre != null) p = først(p.venstre);  // p har høyre subtre
            else if (s.tom()) p = null;               // stakken er tom
            else p = s.taUt();                        // tar fra stakken

            return verdi;                             // returnerer verdien
        }

        @Override
        public boolean hasNext()
        {
            return p != null;
        }

    }

    public Iterator preIterator() {
        return new preordenIterator();
    }

    private class preordenIterator implements Iterator<T>
    {
        private Stakk<Node<T>> s = new TabellStakk<>();
        private Node<T> p = null;
        private int iteratorEndringer = endringer;

        private preordenIterator()          // konstruktør
        {
            if (tom()) return;               // treet er tomt
            p = rot;                  // bruker hjelpemetoden
        }

        @Override
        public T next() {
            if (!hasNext()) throw new NoSuchElementException();
            if (iteratorEndringer != endringer) {
                throw new IllegalArgumentException("Det er gjort endringer siden iteratoren startet");
            }

            T verdi = p.verdi;

            if (p.venstre != null)                  // går til venstre
            {
                if (p.høyre != null) s.leggInn(p.høyre);
                p = p.venstre;
            }
            else if (p.høyre != null) p = p.høyre;  // går til høyre
            else if (s.tom()) p = null;             // ingen flere i treet
            else p = s.taUt();                      // tar fra satkken

            return verdi;
        }

        @Override
        public boolean hasNext()
        {
            return p != null;
        }

    }

} // class BinTre<T>
