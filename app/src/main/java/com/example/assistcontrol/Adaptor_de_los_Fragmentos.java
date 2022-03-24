package com.example.assistcontrol;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class Adaptor_de_los_Fragmentos extends FragmentPagerAdapter {
    public Adaptor_de_los_Fragmentos(FragmentManager fm) {
        super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @Override
    public Fragment getItem(int posicion) {
        Fragment fragmento = null;

        switch (posicion){
            case 0:
                fragmento = new Fragmento_Registro();
                break;
            case 1:
                fragmento = new Fragmento_Marcar();
                break;
            case 2:
                fragmento = new Fragmento_Perfil();
                break;
        }

        return fragmento;
    }

    @Override
    public int getCount() {
        return 3;
    }
}